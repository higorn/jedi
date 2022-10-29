package jedi.injection;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Producer;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProducerFactoryTest {

  private ProducerFactory producerFactory;

  @BeforeEach
  void setUp() {
    JeDI di = new JeDI("jedi.injection");
    producerFactory = new ProducerFactory();
  }

  class A {}
  @Test
  void producerDoesNotExists() {
    assertNull(producerFactory.getProducer(A.class));
  }

  class B {}
  @Produces
  B getB() {
    return new B();
  }
  @Test
  void producerWithoutParameters() {
    Producer<B> producer = producerFactory.getProducer(B.class);
    assertTrue(producer instanceof MethodProducer);
    assertNotNull(producer.produce(null));
  }

  interface C {
    B getB();
  }
  @Produces
  C getC(B b) {
    return () -> b;
  }
  @Test
  void producerWithASingleParameter() {
    Producer<C> producer = producerFactory.getProducer(C.class);
    assertTrue(producer instanceof MethodProducer);
    var c = producer.produce(null);
    assertNotNull(c.getB());
  }
}