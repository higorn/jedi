package jedi.bean;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.util.TypeLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanInstance<T> implements Instance<T> {
  private final Set<Bean<T>>    allBeans;
  private final Type            superType;
  private final Set<Annotation> qualifiers;
  private       Bean<T>         bean;

  public BeanInstance(Set<Bean<T>> allBeans) {
    this(allBeans, null, Set.of());
  }

  public BeanInstance(Set<Bean<T>> allBeans, Type superType, Set<Annotation> qualifiers) {
    this.allBeans = allBeans;
    if (allBeans.size() == 1)
      bean = allBeans.iterator().next();
    this.superType = superType == null ? getSuperType() : superType;
    this.qualifiers = qualifiers;
  }

  private Type getSuperType() {
    if (allBeans.isEmpty())
      return null;
    return allBeans.iterator().next().getBeanClass();
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
  public Handle<T> getHandle() {
    return null;
  }

  @Override
  public Iterable<? extends Handle<T>> handles() {
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
    if (bean != null)
      return bean.create(null);
    bean = getQualifiedBean(qualifiers);
    return bean.create(null);
  }

  public Bean<T> findBean() {
    if (bean != null)
      return bean;
    return getQualifiedBean(qualifiers);
  }

  private Bean<T> getQualifiedBean(Set<Annotation> qualifiers) {
    var qualifiedBeans = allBeans.stream()
        .filter(b -> b.getQualifiers().containsAll(qualifiers))
        .collect(Collectors.toSet());
    if (qualifiedBeans.isEmpty())
      throw new UnsatisfiedResolutionException("No qualified bean found for type "
          + superType.getTypeName() + " with qualifiers " + qualifiers);
    if (qualifiedBeans.size() > 1)
      throw new AmbiguousResolutionException(getAmbiguousResolutionExceptionMesage());

    return qualifiedBeans.iterator().next();
  }

  private String getAmbiguousResolutionExceptionMesage() {
    var msgBuilder = new StringBuilder("Ambiguous dependencies for type ")
        .append(superType.getTypeName())
        .append(" with qualifiers")
        .append("\nPossible dependencies:\n");
    allBeans.forEach(b -> msgBuilder
        .append(" - Managed Bean [")
        .append(b.getBeanClass())
        .append("]")
        .append(" with qualifiers ")
        .append(b.getQualifiers()).append("\n"));
    return msgBuilder.toString();
  }
}
