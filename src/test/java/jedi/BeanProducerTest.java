package jedi;

import jakarta.enterprise.inject.spi.Producer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BeanProducerTest {

  @Test
  void producesAnInstanceFromAMethod() throws NoSuchMethodException {
    var instance = new BeanInstance<>(Set.of(new JediBean<>(getClass(), Set.of())));
    Producer<A> producer = new BeanProducer<>(getClass().getDeclaredMethod("getA"), instance);
    A a = producer.produce(null);
    assertNotNull(a);
  }

  static class A {}

  A getA() {
    return new A();
  }
}