package jedi.resolution.producer;

import jedi.JeDI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResolutionWithProducerTest {
  interface A {
    String say(String msg);
  }
  class B {
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
  @Test
  void producerWithNoParameters() {
    var di = new JeDI("jedi.resolution.producer");
    var b = di.getBean(B.class);
    assertNotNull(b);
  }
}
