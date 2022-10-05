package higor.cdi;

import org.reflections.Reflections;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static higor.cdi.ReflectionHelper.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.util.ReflectionUtilsPredicates.*;

public class BeanInstance<T> implements Instance<T> {
    private Object bean;
    private final Set<?> allBeans;
    private final Reflections reflections;

    public BeanInstance(Class<T> clazz, Reflections reflections) {
        this.reflections = reflections;
        allBeans = resolveBeans(clazz);
        if (allBeans.size() == 1)
            bean = allBeans.iterator().next();
    }

    private Set<?> resolveBeans(Class<T> clazz) {
        if (isAbstraction(clazz))
            return findImplementations(clazz);
        if (hasDefaultConstructorOnly(clazz.getConstructors()))
            return Set.of(newInstanceFromDefaultConstructor(clazz));
        return resolveDependencies(clazz);
    }

    private Set<?> findImplementations(Class<T> clazz) {
        var subTypes = reflections.get(SubTypes.of(clazz).asClass());
        return subTypes.stream()
                .map(c -> selectInstance(c).get())
                .collect(Collectors.toSet());
    }

    private Instance<?> selectInstance(Class<?> c) {
        return new BeanInstance<>(c, reflections);
    }

    private Set<?> resolveDependencies(Class<T> clazz) {
        var constructor = getInjectableConstructor(clazz);
        var resolvedConstructorParams = Arrays.stream(constructor.getParameters())
                .map(this::resolveParameter)
                .collect(Collectors.toList());

        return Set.of(newInstance(constructor, resolvedConstructorParams));
    }

    private Constructor<?> getInjectableConstructor(Class<T> clazz) {
        var constructors = clazz.getConstructors();
        if (constructors.length == 1)
            return constructors[0];
        return Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseThrow(() -> new AmbiguousResolutionException("Ambiguous constructors in class " + clazz.getName()
                        + ". Annotate at least one constructor with the @Inject annotation."));
    }

    private Object resolveParameter(Parameter p) {
        var producers = getProducers(p);
        if (producers.isEmpty())
            return resolveParameterWithoutProducer(p);
        return resolveParamenterWithProducers(producers);
    }

    private Set<Method> getProducers(Parameter p) {
        return reflections.get(MethodsAnnotated.with(Produces.class)
                .as(Method.class)
                .filter(withReturnType(p.getType())));
    }

    private Object resolveParameterWithoutProducer(Parameter p) {
        var className = p.getDeclaringExecutable().getDeclaringClass().getName();
        if (p.getType().isPrimitive())
            throw new UnsatisfiedResolutionException("Unsatisfied dependencies for type "
                    + p.getType().getSimpleName() + " as parameter of " + className);
        return selectInstance(p.getType()).get();
    }

    private Object resolveParamenterWithProducers(Set<Method> producers) {
        var producer = producers.iterator().next();
        return invokeProducer(producer);
    }

    private Object invokeProducer(Method producer) {
        try {
            var producerClassInstance = selectInstance(producer.getDeclaringClass()).get();
            return producer.invoke(producerClassInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Instance<T> select(Annotation... annotations) {
        return null;
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
        return (Instance<U>) selectInstance(aClass);
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        return null;
    }

    @Override
    public boolean isUnsatisfied() {
        return allBeans.isEmpty();
    }

    @Override
    public boolean isAmbiguous() {
        return allBeans.size() > 1;
    }

    @Override
    public void destroy(T t) {

    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public T get() {
        return (T) bean;
    }
}
