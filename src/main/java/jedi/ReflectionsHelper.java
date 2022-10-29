package jedi;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import org.reflections.ReflectionsException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
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

  public static <U> boolean isAbstraction(Class<U> aClass) {
    return aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers());
  }

  public static <U> Constructor<U> getInjectableConstructor(Class<U> subtype) {
    var constructors = (Constructor<U>[]) subtype.getConstructors();
    if (constructors.length == 1)
      return constructors[0];
    return Arrays.stream(constructors)
        .filter(c -> c.isAnnotationPresent(Inject.class))
        .findFirst()
        .orElseThrow(() -> new AmbiguousResolutionException("Ambiguous constructors in class " + subtype.getName()
            + ". Annotate at least one constructor with the @Inject annotation."));
  }

  public static <U> U newInstance(Constructor<U> constructor, List<Object> args) {
    try {
      return constructor.newInstance(args.toArray());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new ReflectionsException(e);
    }
  }

  public static <T> T newInstanceFromDefaultConstructor(Class<T> c) {
    var parameters = c.getDeclaredConstructors()[0].getParameters();
    try {
      if (isDefaultConstructorOfNonStaticInnerClassOrLocalClass(parameters, c))
        return newInnerClassInstance(c, parameters);
      return c.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new ReflectionsException(e);
    }
  }

  private static <T> T newInnerClassInstance(Class<T> c, Parameter[] parameters)
      throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    var type = parameters[0].getType();
    var defaultParamInstance = newInstanceFromDefaultConstructor(type);
    return c.getDeclaredConstructor(type).newInstance(defaultParamInstance);
  }

  public static Set<Annotation> getQualifiers(Parameter p) {
    return getQualifiers(p.getAnnotations());
  }

  public static <T> Set<Annotation> getQualifiers(Class<T> clazz) {
    return getQualifiers(clazz.getAnnotations());
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