package jedi.injection.producer;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static jedi.ReflectionsHelper.newInstance;

public class ConstructorProducer<T> implements Producer<T> {
  private final Constructor<T> constructor;
  private final Set<InjectionPoint> injectionPoints;

  public ConstructorProducer(Constructor<T> constructor, Set<InjectionPoint> injectionPoints) {
    this.constructor = constructor;
    this.injectionPoints = injectionPoints;
  }

  @Override
  public T produce(CreationalContext<T> creationalContext) {
    return newInstance(constructor, getConstructorArgs());
  }

  private List<Object> getConstructorArgs() {
    return injectionPoints.stream().map(this::resolveArgs).collect(Collectors.toList());
  }

  private Object resolveArgs(InjectionPoint injectionPoint) {
    return injectionPoint.getBean().create(null);
  }

  @Override
  public void dispose(T t) {
    //do nothing
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return injectionPoints;
  }
}
