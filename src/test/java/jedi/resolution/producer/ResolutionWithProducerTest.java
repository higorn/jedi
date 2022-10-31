package jedi.resolution.producer;

import jakarta.enterprise.inject.Produces;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResolutionWithProducerTest {

  private JeDI di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.producer");
  }

  interface A {
    String say(String msg);
  }
  public class B {
    private final A a;
    private final String msg;
    public B(A a, String msg) {
      this.a = a;
      this.msg = msg;
    }

    A getA() {
      return a;
    }
  }
  @Produces
  public A getA() {
    return msg -> msg;
  }
  @Produces
  public String getMsg() {
    return "hello";
  }
  @Test
  void producerWithNoParameters() {
    var b = di.getBean(B.class);
    assertNotNull(b);
    assertEquals("hello", b.msg);
    var expectedMsg = "Halo";
    assertEquals(expectedMsg, b.getA().say(expectedMsg));
  }


  interface C {
    String getMsg();
  }
  public class D {
    final C c;

    public D(C c) {
      this.c = c;
    }
  }
  @Produces
  public C getC(String msg) {
    return () -> msg;
  }
  @Test
  void producerWithParameterDependency() {
    var d = di.getBean(D.class);
    assertEquals("hello", d.c.getMsg());
  }
}
