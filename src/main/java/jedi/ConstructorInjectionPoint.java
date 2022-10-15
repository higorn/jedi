package jedi;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

public class ConstructorInjectionPoint implements InjectionPoint {
  private final Set<Annotation> qualifiers;
  private final Bean<?>         bean;

  public ConstructorInjectionPoint(Set<Annotation> qualifiers, Bean<?> bean) {
    this.qualifiers = qualifiers;
    this.bean = bean;
  }

  @Override
  public Type getType() {
    return getClass();
  }

  @Override
  public Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  @Override
  public Bean<?> getBean() {
    return bean;
  }

  @Override
  public Member getMember() {
    return null;
  }

  @Override
  public Annotated getAnnotated() {
    return null;
  }

  @Override
  public boolean isDelegate() {
    return false;
  }

  @Override
  public boolean isTransient() {
    return false;
  }
}
