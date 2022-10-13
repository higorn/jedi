package higor.cdi;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

public class BeanInstance<T> implements Instance<T> {
    private final Set<Bean<T>> allBeans;
    private Bean<T> bean;

    public BeanInstance(Set<Bean<T>> allBeans) {
        this.allBeans = allBeans;
        if (allBeans.size() == 1)
            bean = allBeans.iterator().next();
    }

    @Override
    public Instance<T> select(Annotation... annotations) {
        return null;
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
        return null;
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
        // do nothing
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public T get() {
        return bean.create(null);
    }

    public Bean<T> getBean() {
        return bean;
    }
}
