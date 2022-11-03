package jedi.injection.producer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

import static jedi.ReflectionsHelper.getQualifiers;

public class ProducerHelperPredicates {
  public static <U> String getBeanName(Class<U> type) {
    var simpleName = type.getSimpleName();
    return simpleName.replace(simpleName.substring(0, 1), simpleName.substring(0, 1).toLowerCase());
  }

  public static Predicate<Method> withQualifiers(Set<Annotation> qualifiers) {
    return m -> getQualifiers(m).containsAll(qualifiers);
  }

  public static Predicate<Method> withBeanName(String name) {
    return (input) -> {
      var n = input.getName();
      if (n.startsWith("get"))
        n = n.substring("get".length());
      return name.equals(n.toLowerCase());
    };
  }
}
