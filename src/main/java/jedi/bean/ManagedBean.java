package jedi.bean;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jedi.ReflectionsHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public class ManagedBean<T> implements Bean<T> {
  private final Class<T>        subtype;
  private final Producer<T>     producer;
  private final Set<Annotation> qualifiers;

  public ManagedBean(Class<T> subtype, Producer<T> producer) {
    this.subtype = subtype;
    this.producer = producer;
    this.qualifiers = ReflectionsHelper.getQualifiers(subtype);
  }

  @Override
  public Class<?> getBeanClass() {
    return subtype;
  }

  @Override
  public Set<InjectionPoint> getInjectionPoints() {
    return producer.getInjectionPoints();
  }

  @Override
  public T create(CreationalContext<T> creationalContext) {
    return producer.produce(creationalContext);
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
