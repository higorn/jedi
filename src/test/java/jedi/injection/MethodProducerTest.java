package jedi.injection;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;
import jedi.bean.BeanInstance;
import jedi.bean.ManagedBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MethodProducerTest {

  private Instance<? extends MethodProducerTest> instance;

  @BeforeEach
  void setUp() {
    instance = new BeanInstance<>(Set.of(new ManagedBean<>(getClass(), Set.of())));
  }

  class A {}
  public A getA() {
    return new A();
  }
  @Test
  void methodWithoutArgs() throws NoSuchMethodException {
    var m = getClass().getDeclaredMethod("getA");
    Producer<A> producer = new MethodProducer<>(m, instance, Set.of());
    var a = producer.produce(null);
    assertNotNull(a);
  }

  class B {
    final A a;

    public B(A a) {
      this.a = a;
    }
  }
  public B getB(A a) {
    return new B(a);
  }
  @Test
  void methodWithArgs() throws NoSuchMethodException {
    var getB = getClass().getDeclaredMethod("getB", A.class);
    Producer<B> producer = new MethodProducer<>(getB, instance, getInjectionPoints());
    var b = producer.produce(null);
    assertNotNull(b.a);
  }

  private Set<InjectionPoint> getInjectionPoints() throws NoSuchMethodException {
    var getA = getClass().getDeclaredMethod("getA");
    var producer = new MethodProducer<>(getA, instance, Set.of());
    Bean<?> bean = new ManagedBean(A.class, null, null, producer);
    return Set.of(new ParameterInjectionPoint(Set.of(), bean));
  }
}