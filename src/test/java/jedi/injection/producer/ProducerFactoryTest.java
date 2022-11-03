package jedi.injection.producer;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

public class ProducerFactoryTest {

  private ProducerFactory producerFactory;

  @BeforeEach
  void setUp() {
    JeDI di = new JeDI("jedi.injection");
    producerFactory = new ProducerFactory();
  }

  interface A {}
  @Test
  void producerDoesNotExists() {
    assertNull(producerFactory.createProducer(A.class));
  }

  static class B {}
  @Produces
  public B getB() {
    return new B();
  }
  @Test
  void producerWithoutParameters() {
    Producer<B> producer = producerFactory.createProducer(B.class);
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
    Producer<C> producer = producerFactory.createProducer(C.class);
    assertTrue(producer instanceof MethodProducer);
    var c = producer.produce(null);
    assertNotNull(c.getB());
    assertFalse(c instanceof EC);
  }

  static class D {
    final C c;
    final B b;
    final int n;

    D(C c, B b, int n) {
      this.c = c;
      this.b = b;
      this.n = n;
    }
  }
  @Produces
  int getN() {
    return 10;
  }
  @Produces
  D getD(C c, B b, int n) {
    return new D(c, b, n);
  }
  @Test
  void producerWithMultipleParameters() {
    Producer<D> producer = producerFactory.createProducer(D.class);
    var d = producer.produce(null);
    assertNotNull(d.c.getB());
    assertNotNull(d.b);
    assertEquals(10, d.n);
//    assertSame(d.b, d.c.getB());
  }

  static class EC implements C {
    private final B b;
    EC(B b) {
      this.b = b;
    }
    @Override
    public B getB() {
      return b;
    }
  }
  @Produces @Named
  C getNamedC(B b) {
    return new EC(b);
  }
  @Test
  void producerWitQualifier() throws NoSuchMethodException {
    Producer<C> producer = producerFactory.createProducer(C.class, getNamedQualifier());
    var c = producer.produce(null);
    assertNotNull(c.getB());
    assertTrue(c instanceof EC);
  }

  private Annotation getNamedQualifier() throws NoSuchMethodException {
    Method method = getClass().getDeclaredMethod("getNamedC", B.class);
    return method.getAnnotation(Named.class);
  }


  @Qualifier
  @Retention(RUNTIME)
  @Target({METHOD, FIELD, PARAMETER, TYPE})
  public @interface Sync{}
  @Qualifier
  @Retention(RUNTIME)
  @Target({METHOD, FIELD, PARAMETER, TYPE})
  public @interface Async{}

  interface Payment {}
  @Sync
  public static class SyncPayment implements Payment {}
  @Async
  public static class AsyncPayment implements Payment {}
  public static class PaymentProvider {
    static boolean isAsync = false;
    @Produces
    public Payment getPayment(@Sync Payment syncPayment, @Async Payment asyncPayment) {
      return isAsync ? asyncPayment : syncPayment;
    }
  }
  static class Order {
    final Payment payment;
    Order(Payment payment) {
      this.payment = payment;
    }
  }
  @Produces
  public Order getOrder(Payment payment) {
    return new Order(payment);
  }
  @Test
  void producerWithQualifiedParameters() {
    Producer<Order> producer = producerFactory.createProducer(Order.class);
    var order = producer.produce(null);
    assertNotNull(order.payment);
    assertTrue(order.payment instanceof SyncPayment);

    PaymentProvider.isAsync = true;
    order = producer.produce(null);
    assertTrue(order.payment instanceof AsyncPayment);
  }

  static class OrderConfirmation {
    final Payment payment;
    public OrderConfirmation(Payment payment) {
      this.payment = payment;
    }
  }
  @Test
  void producerConstructor() {
    Producer<OrderConfirmation> producer = producerFactory.createProducer(OrderConfirmation.class);
    assertNotNull(producer);
    assertTrue(producer instanceof ConstructorProducer);
  }
  void ambiguousProducerForTheSameType() {

  }
}