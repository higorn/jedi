package jedi;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import org.reflections.ReflectionsException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class BeanProducer<T> implements Producer<T> {
  private final Method      producerMethod;
  private final Instance<?> producerDeclaringClassInstance;

  public BeanProducer(Method m, Instance<?> instance) {
    producerMethod = m;
    producerDeclaringClassInstance = instance;
  }

  @Override
  public T produce(CreationalContext creationalContext) {
    var producerClassInstance = producerDeclaringClassInstance.get();
    try {
      return (T) producerMethod.invoke(producerClassInstance);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ReflectionsException(e);
    }
  }

  @Override
  public void dispose(Object o) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return Set.of();
  }
}
