package jedi.injection;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
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

import static jedi.ReflectionsHelper.getQualifiers;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;

public class ProducerFactory {
  private final Reflections metadata;
  private final JeDI        jedi;

  public ProducerFactory() {
    this.jedi = (JeDI) CDI.current();
    this.metadata = jedi.getMetadata();
  }

  public <U> Producer<U> getProducer(Class<U> subtype) {
    return metadata.get(MethodsAnnotated.with(Produces.class).as(Method.class)
            .filter(withReturnType(subtype))).stream()
        .map(m -> new MethodProducer<U>(m, jedi.select(m.getDeclaringClass()), getInjectionPoints(m.getParameters())))
        .findFirst()
        .orElse(null);
  }

  private Set<InjectionPoint> getInjectionPoints(Parameter[] parameters) {
    // As this injection points are used to resolve constructor params, they need to be in order,
    // that's why it needs to be an ordered set.
    return Arrays.stream(parameters).map(this::resolveInjectionPoints)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private InjectionPoint resolveInjectionPoints(Parameter p) {
    if (p.getType().isPrimitive()) {
      var className = p.getDeclaringExecutable().getDeclaringClass().getName();
      throw new UnsatisfiedResolutionException(
          "Unsatisfied dependencies for type " + p.getType().getSimpleName() + " as parameter of " + className);
    }
    var qualifiers = getQualifiers(p);
    Instance<?> instance = jedi.select(p.getType(), qualifiers.toArray(new Annotation[] {}));
    Bean<?> bean = ((BeanInstance<?>) instance).findBean();
    return new ParameterInjectionPoint(qualifiers, bean);
  }
}
