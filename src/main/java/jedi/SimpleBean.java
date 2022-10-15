package jedi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.newInstance;
import static jedi.ReflectionsHelper.newInstanceFromDefaultConstructor;

public class SimpleBean<T> implements Bean<T> {
    private final Class<T> subtype;
    private final Set<InjectionPoint> injectionPoints;
    private final Constructor<T> constructor;
    private final Producer<T> producer;
    private final Set<Annotation> qualifiers;

    public SimpleBean(Class<T> subtype, Set<InjectionPoint> injectionPoints) {
        this(subtype, injectionPoints, null);
    }

    public SimpleBean(Class<T> subtype, Set<InjectionPoint> injectionPoints, Constructor<T> constructor) {
        this(subtype, injectionPoints, constructor, null);
    }

    public SimpleBean(Class<T> subtype, Set<InjectionPoint> injectionPoints, Constructor<T> constructor,
                      Producer<T> producer) {
        this.subtype = subtype;
        this.injectionPoints = injectionPoints;
        this.constructor = constructor;
        this.producer = producer;
        this.qualifiers = ReflectionsHelper.getQualifiers(subtype);
    }

    @Override
    public Class<?> getBeanClass() {
        return subtype;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        if (producer != null)
            return producer.produce(null);
        if (constructor == null)
            return newInstanceFromDefaultConstructor(subtype);
        return newInstance(constructor, getConstructorParams());
    }

    private List<Object> getConstructorParams() {
        return injectionPoints.stream()
                .map(this::resolveParameter)
                .collect(Collectors.toList());
    }

    private Object resolveParameter(InjectionPoint injectionPoint) {
        return injectionPoint.getBean().create(null);
    }

    @Override
    public void destroy(T t, CreationalContext<T> creationalContext) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Set<Type> getTypes() {
        return Set.of();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return null;
    }

    @Override
    public String getName() {
        return subtype.getName();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Set.of();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }
}
