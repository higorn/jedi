package jedi.injection;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jedi.JeDI;
import jedi.bean.BeanInstance;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.cast;
import static jedi.ReflectionsHelper.getQualifiers;
import static jedi.injection.ProducerHelperPredicates.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;

public class ProducerFactory {
  private final Reflections metadata;
  private final JeDI        jedi;

  public ProducerFactory() {
    this.jedi = (JeDI) CDI.current();
    this.metadata = jedi.getMetadata();
  }

  public <U> Producer<U> getProducer(Class<U> subtype, Annotation... annotations) {
    return getProducer(subtype, getBeanName(subtype), annotations);
  }

  public <U> Producer<U> getProducer(Class<U> subtype, String beanName, Annotation... annotations) {
    var qualifiers = getQualifiers(annotations);
    Set<Method> methods = metadata.get(MethodsAnnotated.with(Produces.class)
        .as(Method.class)
        .filter(withReturnType(subtype))
        .filter(withQualifiers(qualifiers)));
    if (methods.size() > 1)
      methods = methods.stream().filter(withBeanName(beanName)).collect(Collectors.toSet());
    return cast(methods.stream()
        .map(this::getMethodProducer)
        .findFirst()
        .orElse(null));
  }

  private <U> Producer<U> getMethodProducer(Method m) {
    return new MethodProducer<>(m, jedi.select(m.getDeclaringClass()), getInjectionPoints(m.getParameters()));
  }

  private Set<InjectionPoint> getInjectionPoints(Parameter[] parameters) {
    // As this injection points are used to resolve constructor params, they need to be in order,
    // that's why it needs to be an ordered set.
    return Arrays.stream(parameters).map(this::resolveInjectionPoints)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private InjectionPoint resolveInjectionPoints(Parameter p) {
    var qualifiers = getQualifiers(p);
    Instance<?> instance = jedi.select(p.getType(), qualifiers.toArray(new Annotation[] {}));
    Bean<?> bean = ((BeanInstance<?>) instance).findBean();
    return new ParameterInjectionPoint(qualifiers, bean);
  }
}
