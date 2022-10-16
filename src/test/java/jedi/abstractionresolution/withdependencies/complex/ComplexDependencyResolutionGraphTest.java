package jedi.abstractionresolution.withdependencies.complex;

import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ComplexDependencyResolutionGraphTest {
  interface A {
    E getE();
    D getD();
  }
  public static class B {}
  interface C {
    B getB();
  }
  interface D {}
  interface E {
    B getB();
    C getC();
    F getF();
  }
  public static class Start {
    final A a;
    final B b;
    final C c;
    final D d;
    public Start(A a, B b, C c, D d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
    }
  }
  public static class GA implements A {
    final E e;
    final D d;
    public GA(E e, D d) {
      this.e = e;
      this.d = d;
    }
    @Override
    public E getE() { return e; }
    @Override
    public D getD() { return d; }
  }
  public static class HE implements E {
    final B b;
    final C c;
    final F f;
    public HE(B b, C c, F f) {
      this.b = b;
      this.c = c;
      this.f = f;
    }
    @Override
    public B getB() { return b; }
    @Override
    public C getC() { return c; }
    @Override
    public F getF() { return f; }
  }
  public static class IC implements C {
    final B b;
    public IC(B b) {
      this.b = b;
    }
    @Override
    public B getB() { return b; }
  }
  public static class F {
    final D d;
    public F(D d) {
      this.d = d;
    }
  }
  public static class JD implements D {}

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.abstractionresolution.withdependencies.complex");
  }

  @Test
  void shouldResolveADependencyGraph() {
    var start = di.select(Start.class).get();
    assertNotNull(start.a.getE().getB());
    assertNotNull(start.a.getE().getC().getB());
    assertNotNull(start.a.getE().getF().d);
    assertNotNull(start.a.getD());
    assertNotNull(start.b);
    assertNotNull(start.c.getB());
    assertNotNull(start.d);
  }
}
