package jedi.injection.producer;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jedi.JeDI;
import jedi.bean.BeanInstance;
import jedi.injection.ParameterInjectionPoint;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.*;
import static jedi.injection.producer.ProducerHelperPredicates.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;

public class ProducerFactory {
  private final Reflections metadata;
  private final JeDI        jedi;

  public ProducerFactory() {
    this.jedi = (JeDI) CDI.current();
    this.metadata = jedi.getMetadata();
  }

  public <U> Producer<U> createProducer(Class<U> subtype, Annotation... annotations) {
    return createProducer(subtype, getBeanName(subtype), annotations);
  }

  public <U> Producer<U> createProducer(Class<U> subtype, String beanName, Annotation... annotations) {
    Set<Method> methods = findMethodsProducer(subtype, getQualifiers(annotations));
    if (methods.size() == 0)
      return getConstructorProducer(subtype);
    if (methods.size() > 1)
      methods = methods.stream().filter(withBeanName(beanName)).collect(Collectors.toSet());
    return cast(methods.stream()
        .map(this::getMethodProducer)
        .findFirst()
        .orElse(null));
  }

  private <U> Set<Method> findMethodsProducer(Class<U> subtype, Set<Annotation> qualifiers) {
    return metadata.get(MethodsAnnotated.with(Produces.class)
        .as(Method.class)
        .filter(withReturnType(subtype))
        .filter(withQualifiers(qualifiers)));
  }

  private <U> Producer<U> getConstructorProducer(Class<U> subtype) {
    if (isAbstraction(subtype))
      return null;
    var constructor = getInjectableConstructor(subtype);
    return new ConstructorProducer<>(constructor, getInjectionPoints(constructor.getParameters()));
  }

  private <U> Producer<U> getMethodProducer(Method m) {
    return new MethodProducer<>(m, jedi.select(m.getDeclaringClass()), getInjectionPoints(m.getParameters()));
  }

  private Set<InjectionPoint> getInjectionPoints(Parameter[] parameters) {
    // As this injection points are used to resolve constructor params, they need to be in order,
    // that's why it needs to be an ordered set.
    return Arrays.stream(parameters)
        .map(this::resolveInjectionPoints)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private InjectionPoint resolveInjectionPoints(Parameter p) {
    var qualifiers = getQualifiers(p);
    Instance<?> instance = jedi.select(p.getType(), qualifiers.toArray(new Annotation[] {}));
    Bean<?> bean = ((BeanInstance<?>) instance).findBean();
    return new ParameterInjectionPoint(qualifiers, bean);
  }
}
