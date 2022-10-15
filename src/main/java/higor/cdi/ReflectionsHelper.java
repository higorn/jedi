package higor.cdi;

import org.reflections.ReflectionsException;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Qualifier;
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
    public static boolean hasDefaultConstructorOnly(Constructor<?>[] constructors) {
        if (constructors.length == 0)
            return true;
        if (constructors.length == 1) {
            Parameter[] parameters = constructors[0].getParameters();
            return parameters.length == 0 || isDefaultParameterOfInnerClassEmptyConstructor(parameters);
        }
        return false;
    }

    public static boolean isDefaultParameterOfInnerClassEmptyConstructor(Parameter[] parameters) {
        return parameters.length == 1 && parameters[0].getName().equals("arg0");
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
                .orElseThrow(() -> new AmbiguousResolutionException("Ambiguous constructors in class "
                        + subtype.getName() + ". Annotate at least one constructor with the @Inject annotation."));
    }

    public static <U> U newInstance(Constructor<U> constructor, List<Object> params) {
        try {
            return constructor.newInstance(params.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionsException(e);
        }
    }

    public static <T> T newInstanceFromDefaultConstructor(Class<T> c) {
        var parameters = c.getDeclaredConstructors()[0].getParameters();
        try {
            if (isDefaultParameterOfInnerClassEmptyConstructor(parameters))
                return newInnerClassInstance(c, parameters);
            return c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ReflectionsException(e);
        }
    }

    private static <T> T newInnerClassInstance(Class<T> c, Parameter[] parameters)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var param0 = parameters[0];
        var type = param0.getType();
        var o = newInstanceFromDefaultConstructor(type);
        return c.getDeclaredConstructor(type).newInstance(o);
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
}
