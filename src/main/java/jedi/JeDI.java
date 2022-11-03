package jedi;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;
import jedi.bean.BeanInstance;
import jedi.bean.ManagedBean;
import jedi.injection.producer.ProducerFactory;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.*;
import static org.reflections.scanners.Scanners.SubTypes;

public class JeDI extends CDI<Object> {
  private final Set<QualifiedType<?>>              seen = new HashSet<>();
  private final Map<QualifiedType<?>, Instance<?>> cache = new ConcurrentHashMap<>();
  private final ProducerFactory                    producerFactory;
  private final Reflections                        metadata;

  public JeDI(String prefix) {
    this(prefix, Scanners.values());
  }

  public JeDI(String prefix, Scanners... scanners) {
    setCDIProvider(() -> this);
    metadata = new Reflections(prefix, scanners);
    producerFactory = new ProducerFactory();
  }

  public Reflections getMetadata() {
    return metadata;
  }

  public <U> U getBean(Class<U> subtype, Annotation... annotations) {
    return select(subtype, annotations).get();
  }

  @Override
  public BeanManager getBeanManager() {
    return null;
  }

  @Override
  public Instance<Object> select(Annotation... annotations) {
    return null;
  }

  @Override
  public <U> Instance<U> select(Class<U> subtype, Annotation... annotations) {
    Set<Annotation> qualifiers = getQualifiers(annotations);
    var qualifiedType = new QualifiedType<>(subtype, qualifiers);
    if (cache.containsKey(qualifiedType))
      return cast(cache.get(qualifiedType));
    if (seen.contains(qualifiedType))
      throw new CircularDependencyException("Circular dependency detected on type [" + subtype.toString() + "]");
    seen.add(qualifiedType);
    var instance = resolveInstance(subtype, qualifiers);
    cache.put(qualifiedType, instance);
    seen.remove(qualifiedType);
    return instance;
  }

  public <U> Instance<U> resolveInstance(Class<U> subtype, Set<Annotation> qualifiers) {
    var producer = producerFactory.createProducer(subtype, qualifiers.toArray(new Annotation[]{}));
    if (producer == null && isAbstraction(subtype))
      return new BeanInstance<>(findImplementations(subtype), subtype, qualifiers);
    return new BeanInstance<>(Set.of(new ManagedBean<>(subtype, producer)));
  }

  @SuppressWarnings("unchecked")
  private <U> Set<Bean<U>> findImplementations(Class<U> subtype) {
    var subTypes = metadata.get(SubTypes.of(subtype).asClass());
    return subTypes.stream()
        .map(c -> ((BeanInstance<U>) select(c)).findBean())
        .collect(Collectors.toSet());
  }

  @Override
  public <U> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
    return null;
  }

  @Override
  public boolean isUnsatisfied() {
    return false;
  }

  @Override
  public boolean isAmbiguous() {
    return false;
  }

  @Override
  public void destroy(Object o) {

  }

  @Override
  public Handle<Object> getHandle() {
    return null;
  }

  @Override
  public Iterable<? extends Handle<Object>> handles() {
    return null;
  }

  @Override
  public Object get() {
    return null;
  }

  @Override
  public Iterator<Object> iterator() {
    return null;
  }

  static class QualifiedType<T> {
    private final Class<T> type;
    private final Set<Annotation> qualifiers;

    public QualifiedType(Class<T> type, Set<Annotation> qualifiers) {
      this.type = type;
      this.qualifiers = qualifiers;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof QualifiedType))
        return false;
      QualifiedType<T> other = cast(obj);
      return this.type.equals(other.type) && this.qualifiers.equals(other.qualifiers);
    }

    @Override
    public int hashCode() {
      return type != null && qualifiers != null
          ? type.hashCode() + qualifiers.stream()
          .map(Annotation::annotationType)
          .map(Class::hashCode)
          .reduce(0, Integer::sum)
          : super.hashCode();
    }
  }

  public static class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String message) {
      super(message);
    }
  }
}
