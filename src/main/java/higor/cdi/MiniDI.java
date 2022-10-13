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
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static higor.cdi.ReflectionHelper.*;
import static org.reflections.scanners.Scanners.MethodsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.util.ReflectionUtilsPredicates.withReturnType;


public class MiniDI extends CDI<Object> {
    private final Reflections metadata;
    private final Set<Class<?>> visited = new HashSet<>();

    public MiniDI(String prefix) {
        this(prefix, Scanners.values());
    }

    public MiniDI(String prefix, Scanners... scanners) {
        metadata = new Reflections(prefix, scanners);
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
        if (visited.contains(subtype))
            throw new CircularDependencyException();
        visited.add(subtype);
        BeanInstance<U> instance = resolveInstance(subtype);
        visited.remove(subtype);
        return instance;
    }

    private <U> BeanInstance<U> resolveInstance(Class<U> subtype) {
        BeanInstance<U> instance;
        if (isAbstraction(subtype))
            instance = new BeanInstance<>(findImplementations(subtype));
        else if (hasDefaultConstructorOnly(subtype.getConstructors()))
            instance = new BeanInstance<>(Set.of(newInstanceFromDefaultConstructor(subtype)));
        else
            instance = new BeanInstance<>(resolveDependencies(subtype));
        return instance;
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        return null;
    }

    private <U> Set<U> findImplementations(Class<U> subtype) {
        var subTypes = metadata.get(SubTypes.of(subtype).asClass());
        return subTypes.stream()
                .map(c -> (U) select(c).get())
                .collect(Collectors.toSet());
    }

    private <U> Set<U> resolveDependencies(Class<U> subtype) {
        var constructor = getInjectableConstructor(subtype);
        var resolvedConstructorParams = Arrays.stream(constructor.getParameters())
                .map(this::resolveParameter)
                .collect(Collectors.toList());

        return Set.of((U)newInstance(constructor, resolvedConstructorParams));
    }

    private Constructor<?> getInjectableConstructor(Class<?> subtype) {
        var constructors = subtype.getConstructors();
        if (constructors.length == 1)
            return constructors[0];
        return Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseThrow(() -> new AmbiguousResolutionException("Ambiguous constructors in class " + subtype.getName()
                        + ". Annotate at least one constructor with the @Inject annotation."));
    }

    private Object resolveParameter(Parameter p) {
        var producers = getProducers(p);
        if (producers.isEmpty())
            return resolveParameterWithoutProducer(p);
        return resolveParamenterWithProducers(producers);
    }

    private Set<Method> getProducers(Parameter p) {
        return metadata.get(MethodsAnnotated.with(Produces.class)
                .as(Method.class)
                .filter(withReturnType(p.getType())));
    }

    private Object resolveParameterWithoutProducer(Parameter p) {
        var qualifiers = getQualifiers(p);
        var className = p.getDeclaringExecutable().getDeclaringClass().getName();
        if (p.getType().isPrimitive())
            throw new UnsatisfiedResolutionException("Unsatisfied dependencies for type "
                    + p.getType().getSimpleName() + " as parameter of " + className);
        return select(p.getType(), qualifiers).get();
//        return select(p.getType()).get();
    }

    private Annotation[] getQualifiers(Parameter p) {
        return Arrays.stream(p.getAnnotations())
                .filter(a -> metadata.get(SubTypes.of(Qualifier.class).as(Annotation.class)).stream()
                        .anyMatch(q -> q.equals(a)))
                .collect(Collectors.toSet())
                .toArray(new Annotation[]{});
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

    public static class CircularDependencyException extends RuntimeException {
    }
}
