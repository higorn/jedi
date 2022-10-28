package jedi.resolution.abstraction.withdependencies.complex;

import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import jedi.bean.BeanInstance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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

  @Test
  void shouldResolveADependencyGraph() {
    di = new JeDI("jedi.resolution.abstraction.withdependencies.complex");
    var start = di.select(Start.class).get();
    assertNotNull(start.a.getE().getB());
    assertNotNull(start.a.getE().getC().getB());
    assertNotNull(start.a.getE().getF().d);
    assertNotNull(start.a.getD());
    assertNotNull(start.b);
    assertNotNull(start.c.getB());
    assertNotNull(start.d);
  }

  @Test
  void shouldCacheVisitedNodes() {
    di = new JeDI("jedi.resolution.abstraction.withdependencies.complex");
    var instance = (BeanInstance<Start>) di.select(Start.class);
    var startBean = instance.findBean();
    var startIterator = startBean.getInjectionPoints().iterator();
    var aBean = startIterator.next().getBean();
    var aIterator = aBean.getInjectionPoints().iterator();
    var eBean = aIterator.next().getBean();
    var eIterator = eBean.getInjectionPoints().iterator();
    var bBean = eIterator.next().getBean();
    var bBean2 = startIterator.next().getBean();

    assertSame(bBean, bBean2);
    var instanceB = (BeanInstance<B>) di.select(B.class);
    var beanB= instanceB.findBean();
    assertSame(bBean, beanB);
  }
}
