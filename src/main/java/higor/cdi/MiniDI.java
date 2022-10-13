package higor.cdi;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static higor.cdi.ReflectionsHelper.hasDefaultConstructorOnly;
import static higor.cdi.ReflectionsHelper.isAbstraction;
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
            instance = new BeanInstance<>(Set.of(new SimpleBean<>(subtype, Set.of())));
        else
            instance = new BeanInstance<>(resolveDependencies(subtype));
        return instance;
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        return null;
    }

    private <U> Set<Bean<U>> findImplementations(Class<U> subtype) {
        var subTypes = metadata.get(SubTypes.of(subtype).asClass());
        return subTypes.stream()
                .map(c -> ((BeanInstance<U>) select(c)).getBean())
                .collect(Collectors.toSet());
    }

    private <U> Set<Bean<U>> resolveDependencies(Class<U> subtype) {
        var producer = getProducer(subtype);
        if (producer == null) {
            var constructor = getInjectableConstructor(subtype);
            return Set.of(new SimpleBean<>(subtype, getInjectionPoints(constructor.getParameters()), constructor));
        }
        return Set.of(new SimpleBean<>(subtype, null, null, producer));
    }

    private <U> Producer<U> getProducer(Class<U> subtype) {
        return metadata.get(MethodsAnnotated.with(Produces.class)
                .as(Method.class)
                .filter(withReturnType(subtype))).stream()
                .map(m -> new SimpleProducer<U>(m, select(m.getDeclaringClass())))
                .findFirst()
                .orElse(null);
    }

    private <U> Constructor<U> getInjectableConstructor(Class<U> subtype) {
        var constructors = (Constructor<U>[]) subtype.getConstructors();
        if (constructors.length == 1)
            return constructors[0];
        return Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseThrow(() -> new AmbiguousResolutionException("Ambiguous constructors in class "
                        + subtype.getName() + ". Annotate at least one constructor with the @Inject annotation."));
    }

    private Set<InjectionPoint> getInjectionPoints(Parameter[] parameters) {
        // As this injection points are used to resolve constructor params, they need to be in order,
        // that's why it needs to be a ordered set.
        return Arrays.stream(parameters)
                .map(this::resolveInjectionPoints)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private InjectionPoint resolveInjectionPoints(Parameter p) {
        Annotation[] qualifiers = getQualifiers(p);
        var className = p.getDeclaringExecutable().getDeclaringClass().getName();
        if (p.getType().isPrimitive())
            throw new UnsatisfiedResolutionException("Unsatisfied dependencies for type "
                    + p.getType().getSimpleName() + " as parameter of " + className);
        Bean<?> bean = ((BeanInstance<?>) select(p.getType(), qualifiers)).getBean();
        return new ConstructorInjectionPoint(Set.of(qualifiers), bean);
    }

    private Annotation[] getQualifiers(Parameter p) {
        return Arrays.stream(p.getAnnotations())
                .filter(a -> metadata.get(SubTypes.of(Qualifier.class).as(Annotation.class)).stream()
                        .anyMatch(q -> q.equals(a)))
                .collect(Collectors.toSet())
                .toArray(new Annotation[]{});
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
