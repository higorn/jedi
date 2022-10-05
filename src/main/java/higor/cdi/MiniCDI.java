package higor.cdi;

import org.reflections.Reflections;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import static higor.cdi.ReflectionHelper.hasDefaultConstructorOnly;
import static higor.cdi.ReflectionHelper.isAbstraction;


public class MiniCDI extends CDI<Object> {
    private final Reflections reflections;

    public MiniCDI(String prefix) {
        reflections = new Reflections(prefix);
        setCDIProvider(() -> this);
    }

    public <T> T get(Class<T> classAClass) {
        return null;
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
        return new BeanInstance<>(subtype, reflections);
/*
        if (isAbstraction(subtype))
            return findImplementations(subtype);
        if (hasDefaultConstructorOnly(subtype.getConstructors()))
            return Set.of(newInstanceFromDefaultConstructor(subtype));
        return resolveDependencies(clazz);
*/
    }

    @Override
    public <U> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        return null;
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
