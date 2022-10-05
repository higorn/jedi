package higor.cdi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;

public class ReflectionHelper {

    private ReflectionHelper() {}
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

    public static Object newInstance(Constructor<?> constructor, List<Object> params) {
        try {
            return constructor.newInstance(params.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstanceFromDefaultConstructor(Class<T> c) {
        var parameters = c.getDeclaredConstructors()[0].getParameters();
        try {
            if (isDefaultParameterOfInnerClassEmptyConstructor(parameters))
                return newInnerClassInstance(c, parameters);
            return c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T newInnerClassInstance(Class<T> c, Parameter[] parameters)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var param0 = parameters[0];
        var type = param0.getType();
        var o = newInstanceFromDefaultConstructor((Class<T>) type);
        return c.getDeclaredConstructor(type).newInstance(o);
    }
}
