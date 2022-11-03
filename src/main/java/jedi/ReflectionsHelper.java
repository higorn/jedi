package jedi;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import org.reflections.ReflectionsException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionsHelper {

  private ReflectionsHelper() {}

  public static boolean hasDefaultConstructorOnly(Class<?> subtype) {
    var constructors = subtype.getConstructors();
    if (constructors.length == 0)
      return true;
    if (constructors.length == 1) {
      var parameters = constructors[0].getParameters();
      return parameters.length == 0 || isDefaultConstructorOfNonStaticInnerClassOrLocalClass(parameters, subtype);
    }

    return false;
  }

  public static boolean isDefaultConstructorOfNonStaticInnerClassOrLocalClass(Parameter[] parameters,
      Class<?> declaringClass) {
    return parameters.length == 1
        && (declaringClass.isLocalClass() || parameters[0].getType().equals(declaringClass.getDeclaringClass()));
  }

  public static <T> boolean isAbstraction(Class<T> aClass) {
    return aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers());
  }

  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> getInjectableConstructor(Class<T> subtype) {
    if (hasDefaultConstructorOnly(subtype))
      return cast(subtype.getDeclaredConstructors()[0]);
    var constructors = (Constructor<T>[]) subtype.getConstructors();
    if (constructors.length > 1)
      return Arrays.stream(constructors)
          .filter(c -> c.isAnnotationPresent(Inject.class))
          .findFirst()
          .orElseThrow(() -> new AmbiguousResolutionException("Ambiguous constructors in class " + subtype.getName()
              + ". Annotate at least one constructor with the @Inject annotation."));
    return constructors[0];
  }

  public static <T> T newInstance(Constructor<T> constructor, List<Object> args) {
    try {
      return constructor.newInstance(args.toArray());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new ReflectionsException(e);
    }
  }

  public static Set<Annotation> getQualifiers(Parameter p) {
    return getQualifiers(p.getAnnotations());
  }

  public static <T> Set<Annotation> getQualifiers(Class<T> clazz) {
    return getQualifiers(clazz.getAnnotations());
  }

  public static <T> Set<Annotation> getQualifiers(Method method) {
    return getQualifiers(method.getAnnotations());
  }

  public static Set<Annotation> getQualifiers(Annotation... annotations) {
    var qualifiers = Arrays.stream(annotations)
        .filter(a -> a.annotationType().getAnnotation(Qualifier.class) != null)
        .collect(Collectors.toSet());
    qualifiers.add(Any.Literal.INSTANCE);
    qualifiers.add(Default.Literal.INSTANCE);
    return qualifiers;
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object obj) {
    return (T) obj;
  }
}