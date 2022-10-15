package jedi;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.TypeLiteral;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;

public class JeDI extends CDI<Object> {
  private final Set<Class<?>> visited = new HashSet<>();
  private final InstanceResolver instanceResolver;

  public JeDI(String prefix) {
    this(prefix, Scanners.values());
  }

  public JeDI(String prefix, Scanners... scanners) {
    setCDIProvider(() -> this);
    instanceResolver = new InstanceResolver(new Reflections(prefix, scanners));
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
    if (visited.contains(subtype))
      throw new CircularDependencyException();
    visited.add(subtype);
    BeanInstance<U> instance = instanceResolver.resolveInstance(subtype, getQualifiers(annotations));
    visited.remove(subtype);
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

  static class InstanceResolver {
    private final Reflections metadata;
    private final CDI<Object> cdi;

    InstanceResolver(Reflections metadata) {
      this.metadata = metadata;
      this.cdi = CDI.current();
    }

    public <U> BeanInstance<U> resolveInstance(Class<U> subtype, Set<Annotation> qualifiers) {
      BeanInstance<U> instance;
      if (isAbstraction(subtype))
        instance = new BeanInstance<>(findImplementations(subtype), subtype, qualifiers);
      else if (hasDefaultConstructorOnly(subtype.getConstructors()))
        instance = new BeanInstance<>(Set.of(new JediBean<>(subtype, Set.of())));
      else
        instance = new BeanInstance<>(resolveDependencies(subtype));
      return instance;
    }

    private <U> Set<Bean<U>> findImplementations(Class<U> subtype) {
      var subTypes = metadata.get(SubTypes.of(subtype).asClass());
      return subTypes.stream()
          .map(c -> ((BeanInstance<U>) cdi.select(c)).findBean())
          .collect(Collectors.toSet());
    }

    private <U> Set<Bean<U>> resolveDependencies(Class<U> subtype) {
      var producer = getProducer(subtype);
      if (producer == null) {
        var constructor = getInjectableConstructor(subtype);
        return Set.of(new JediBean<>(subtype, getInjectionPoints(constructor.getParameters()), constructor));
      }
      return Set.of(new JediBean<>(subtype, null, null, producer));
    }

    private <U> Producer<U> getProducer(Class<U> subtype) {
      return metadata.get(MethodsAnnotated.with(Produces.class).as(Method.class)
              .filter(withReturnType(subtype))).stream()
          .map(m -> new BeanProducer<U>(m, cdi.select(m.getDeclaringClass())))
          .findFirst()
          .orElse(null);
    }

    private Set<InjectionPoint> getInjectionPoints(Parameter[] parameters) {
      // As this injection points are used to resolve constructor params, they need to be in order,
      // that's why it needs to be a ordered set.
      return Arrays.stream(parameters)
          .map(this::resolveInjectionPoints)
          .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private InjectionPoint resolveInjectionPoints(Parameter p) {
      var qualifiers = getQualifiers(p);
      var className = p.getDeclaringExecutable().getDeclaringClass().getName();
      if (p.getType().isPrimitive())
        throw new UnsatisfiedResolutionException("Unsatisfied dependencies for type " + p.getType().getSimpleName()
            + " as parameter of " + className);
      Instance<?> instance = cdi.select(p.getType(), qualifiers.toArray(new Annotation[] {}));
      Bean<?> bean = ((BeanInstance<?>) instance).findBean();
      return new ConstructorInjectionPoint(qualifiers, bean);
    }
  }

  public static class CircularDependencyException extends RuntimeException {
  }
}
