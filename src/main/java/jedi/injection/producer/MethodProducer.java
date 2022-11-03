package jedi.injection.producer;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import org.reflections.ReflectionsException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static jedi.ReflectionsHelper.cast;

public class MethodProducer<T> implements Producer<T> {
  private final Method      producerMethod;
  private final Instance<?> producerDeclaringClassInstance;
  private final Set<InjectionPoint> injectionPoints;

  public MethodProducer(Method m, Instance<?> instance, Set<InjectionPoint> injectionPoints) {
    producerMethod = m;
    producerDeclaringClassInstance = instance;
    this.injectionPoints = injectionPoints;
  }

  @Override
  public T produce(CreationalContext creationalContext) {
    var producerClassInstance = producerDeclaringClassInstance.get();
    try {
      return cast(producerMethod.invoke(producerClassInstance, getMethodArgs()));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new ReflectionsException(e);
    }
  }

  private Object[] getMethodArgs() {
    return injectionPoints.stream().map(this::resolveArgs).toArray();
  }

  private Object resolveArgs(InjectionPoint injectionPoint) {
    return injectionPoint.getBean().create(null);
  }

  @Override
  public void dispose(Object o) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return injectionPoints;
  }
}
