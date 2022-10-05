package higor.cdi;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static higor.cdi.ReflectionHelper.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;


public class MiniCDI extends CDI<Object> {
    private final Reflections reflections;

    public MiniCDI(String prefix) {
        this(prefix, Scanners.values());
    }

    public MiniCDI(String prefix, Scanners... scanners) {
        reflections = new Reflections(prefix, scanners);
        setCDIProvider(() -> this);
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
        if (isAbstraction(subtype))
            return new BeanInstance<>(findImplementations(subtype));
        if (hasDefaultConstructorOnly(subtype.getConstructors()))
            return new BeanInstance<>(Set.of(newInstanceFromDefaultConstructor(subtype)));
        return new BeanInstance<>(resolveDependencies(subtype));
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        return null;
    }

    private <U> Set<U> findImplementations(Class<U> clazz) {
        var subTypes = reflections.get(SubTypes.of(clazz).asClass());
        return subTypes.stream()
                .map(c -> (U) select(c).get())
                .collect(Collectors.toSet());
    }

    private <U> Set<U> resolveDependencies(Class<U> clazz) {
        var constructor = getInjectableConstructor(clazz);
        var resolvedConstructorParams = Arrays.stream(constructor.getParameters())
                .map(this::resolveParameter)
                .collect(Collectors.toList());

        return Set.of((U)newInstance(constructor, resolvedConstructorParams));
    }

    private Constructor<?> getInjectableConstructor(Class<?> clazz) {
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
        return select(p.getType()).get();
    }

    private Object resolveParamenterWithProducers(Set<Method> producers) {
        var producer = producers.iterator().next();
        return invokeProducer(producer);
    }

    private Object invokeProducer(Method producer) {
        try {
            var producerClassInstance = select(producer.getDeclaringClass()).get();
            return producer.invoke(producerClassInstance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
}
