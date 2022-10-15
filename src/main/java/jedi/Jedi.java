package jedi;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;

public class Jedi extends CDI<Object> {
  private final Reflections   metadata;

  private final Set<Class<?>> visited = new HashSet<>();

  public Jedi(String prefix) {
    this(prefix, Scanners.values());
  }

  public Jedi(String prefix, Scanners... scanners) {
    metadata = new Reflections(prefix, scanners);
    setCDIProvider(() -> this);
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
    BeanInstance<U> instance = resolveInstance(subtype, getQualifiers(annotations));
    visited.remove(subtype);
    return instance;
  }

  private <U> BeanInstance<U> resolveInstance(Class<U> subtype, Set<Annotation> qualifiers) {
    BeanInstance<U> instance;
    if (isAbstraction(subtype))
      instance = new BeanInstance<>(findImplementations(subtype, qualifiers), subtype);
    else if (hasDefaultConstructorOnly(subtype.getConstructors()))
      instance = new BeanInstance<>(Set.of(new JediBean<>(subtype, Set.of())));
    else
      instance = new BeanInstance<>(resolveDependencies(subtype));
    return instance;
  }

  @Override
  public <U> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
    return null;
  }

  private <U> Set<Bean<U>> findImplementations(Class<U> subtype, Set<Annotation> qualifiers) {
    var subTypes = metadata.get(SubTypes.of(subtype).asClass());
    return subTypes.stream()
        .map(c -> ((BeanInstance<U>) select(c)).findBean(qualifiers))
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
        .map(m -> new BeanProducer<U>(m, select(m.getDeclaringClass())))
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
    Instance<?> instance = select(p.getType(), qualifiers.toArray(new Annotation[] {}));
    Bean<?> bean = ((BeanInstance<?>) instance).findBean(qualifiers);
    return new ConstructorInjectionPoint(qualifiers, bean);
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
    // do nothing
  }

  @Override
  public Iterator<Object> iterator() {
    return null;
  }

  @Override
  public Object get() {
    return null;
  }

  public static class CircularDependencyException extends RuntimeException {
  }
}