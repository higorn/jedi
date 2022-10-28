package jedi;

import jakarta.enterprise.inject.spi.Producer;
import jedi.bean.BeanInstance;
import jedi.bean.ManagedBean;
import jedi.injection.MethodProducer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MethodProducerTest {

  @Test
  void producesAnInstanceFromAMethod() throws NoSuchMethodException {
    var instance = new BeanInstance<>(Set.of(new ManagedBean<>(getClass(), Set.of())));
    Producer<A> producer = new MethodProducer<>(getClass().getDeclaredMethod("getA"), instance);
    A a = producer.produce(null);
    assertNotNull(a);
  }

  static class A {}

  public A getA() {
    return new A();
  }
}