package jedi.bean;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jedi.injection.ParameterInjectionPoint;
import jedi.injection.producer.ConstructorProducer;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static jedi.ReflectionsHelper.getInjectableConstructor;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BeanInstanceTest {

  public class A {}
  @Test
  void aBeanWithNoDependencies() {
    var injectionPoint0 = getInjectionPoint(BeanInstanceTest.class, Set.of());
    Producer<A> producer = new ConstructorProducer<>(getInjectableConstructor(A.class), Set.of(injectionPoint0));
    Bean<A> bean = new ManagedBean<>(A.class, producer);
    Instance<A> instance = new BeanInstance<>(Set.of(bean));
    var a = instance.get();
    assertNotNull(a);
  }

  public class B {}
  public class C {
    final B b;

    public C(B b) {
      this.b = b;
    }
  }
  @Test
  void aBeanWithOneDependency() {
    var injectionPoint0 = getInjectionPoint(BeanInstanceTest.class, Set.of());
    var injectionPointB = getInjectionPoint(B.class, Set.of(injectionPoint0));
    LinkedHashSet<InjectionPoint> injectionPoints = new LinkedHashSet<>();
    injectionPoints.add(injectionPoint0);
    injectionPoints.add(injectionPointB);

    Producer<C> producerC = new ConstructorProducer<>(getInjectableConstructor(C.class), injectionPoints);
    Bean<C> bean = new ManagedBean<>(C.class, producerC);
    Instance<C> instance = new BeanInstance<>(Set.of(bean));
    var c = instance.get();

    assertNotNull(c);
    assertNotNull(c.b);
  }

  private <T> InjectionPoint getInjectionPoint(Class<T> type, Set<InjectionPoint> injectionPoints) {
    Producer<T> producer0 = new ConstructorProducer<>(getInjectableConstructor(type), injectionPoints);
    return new ParameterInjectionPoint(Set.of(), new ManagedBean<>(type, producer0));
  }
}
