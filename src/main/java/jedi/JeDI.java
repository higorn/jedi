package jedi;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;
import jedi.bean.BeanInstance;
import jedi.bean.ManagedBean;
import jedi.injection.ParameterInjectionPoint;
import jedi.injection.ProducerFactory;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.*;
import static org.reflections.scanners.Scanners.SubTypes;

public class JeDI extends CDI<Object> {
  private final Set<Class<?>>              seen = new HashSet<>();
  private final Map<Class<?>, Instance<?>> cache = new ConcurrentHashMap<>();
  private final InstanceResolver           instanceResolver;
  private final Reflections                metadata;

  public JeDI(String prefix) {
    this(prefix, Scanners.values());
  }

  public JeDI(String prefix, Scanners... scanners) {
    setCDIProvider(() -> this);
    metadata = new Reflections(prefix, scanners);
    instanceResolver = new InstanceResolver(new ProducerFactory());
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
    if (cache.containsKey(subtype))
      return cast(cache.get(subtype));
    if (seen.contains(subtype))
      throw new CircularDependencyException("Circular dependency detected on type [" + subtype.toString() + "]");
    seen.add(subtype);
    var instance = instanceResolver.resolveInstance(subtype, getQualifiers(annotations));
    cache.put(subtype, instance);
    seen.remove(subtype);
    return instance;
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

  class InstanceResolver {
    private final CDI<Object> cdi;
    private final ProducerFactory producerFactory;

    InstanceResolver(ProducerFactory producerFactory) {
      this.producerFactory = producerFactory;
      this.cdi = CDI.current();
    }

    public <U> Instance<U> resolveInstance(Class<U> subtype, Set<Annotation> qualifiers) {
      var producer = producerFactory.getProducer(subtype);
      if (producer != null)
        return new BeanInstance<>(Set.of(new ManagedBean<>(subtype, null, null, producer)));
      if (isAbstraction(subtype))
        return new BeanInstance<>(findImplementations(subtype), subtype, qualifiers);
      if (hasDefaultConstructorOnly(subtype))
        return new BeanInstance<>(Set.of(new ManagedBean<>(subtype, Set.of())));
      return new BeanInstance<>(resolveDependencies(subtype));
    }

    private <U> Set<Bean<U>> findImplementations(Class<U> subtype) {
      var subTypes = metadata.get(SubTypes.of(subtype).asClass());
      return subTypes.stream()
          .map(c -> ((BeanInstance<U>) cdi.select(c)).findBean())
          .collect(Collectors.toSet());
    }

    private <U> Set<Bean<U>> resolveDependencies(Class<U> subtype) {
      var constructor = getInjectableConstructor(subtype);
      return Set.of(new ManagedBean<>(subtype, getInjectionPoints(constructor.getParameters()), constructor));
    }

    private Set<InjectionPoint> getInjectionPoints(Parameter[] parameters) {
      // As this injection points are used to resolve constructor params, they need to be in order,
      // that's why it needs to be an ordered set.
      return Arrays.stream(parameters)
          .map(this::resolveInjectionPoints)
          .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private InjectionPoint resolveInjectionPoints(Parameter p) {
      if (p.getType().isPrimitive()) {
        var className = p.getDeclaringExecutable().getDeclaringClass().getName();
        throw new UnsatisfiedResolutionException("Unsatisfied dependencies for type " + p.getType().getSimpleName()
            + " as parameter of " + className);
      }
      var qualifiers = getQualifiers(p);
//      var producer = getProducer(p.getType());
//      if (producer == null) {
        Instance<?> instance = cdi.select(p.getType(), qualifiers.toArray(new Annotation[] {}));
        Bean<?> bean = ((BeanInstance<?>) instance).findBean();
        return new ParameterInjectionPoint(qualifiers, bean);
//      }
//      Bean<?> bean = new ManagedBean(p.getType(), null, null, producer);
//      return new ParameterInjectionPoint(qualifiers, bean);
    }
  }

  public static class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String message) {
      super(message);
    }
  }
}
