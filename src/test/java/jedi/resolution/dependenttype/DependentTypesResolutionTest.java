package jedi.resolution.dependenttype;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.inject.Inject;
import jedi.JeDI;
import jedi.bean.BeanInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DependentTypesResolutionTest {

  private JeDI di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.dependenttype");
  }

  @Test
  void aClassWithMultipleConstructorsThatCanBeResolved_andWithAnUnresolvableDependency() {
    class A {
      public A() {}
      @Inject
      public A(String str) {}
    }
    var exception = assertThrows(AmbiguousResolutionException.class, () -> di.select(A.class));
    assertTrue(exception.getMessage().contains("Ambiguous constructors"));
  }

  public static class Z {
    public Z() {}
    @Inject
    public Z(B b) {}
  }
  @Test
  void aClassWithMultipleConstructorsThatCanBeResolved_andWithAResolvableDependency() {
    var instance = di.select(Z.class);
    assertNotNull(instance.get());
  }

  interface Dependency {}
  public static class Dependent {
    public Dependent(Dependency dependency) {}
  }
  @Test
  void aClassWithAnUnresolvableDependency() {
    var exception = assertThrows(UnsatisfiedResolutionException.class,
        () -> di.getBean(Dependent.class));
    assertTrue(exception.getMessage().contains("No qualified bean found for type " + Dependency.class.getTypeName()));
  }


  /*
   *    |----------------------------
   *    |                           |
   *    |                           v
   *  start ----> A -----> E -----> C
   *    |         |        | \     /
   *    |         v        |  \   /
   *    |-------->D        |   \ /
   *    |         ^        v    v
   *    |         |--------F    B
   *    |                       ^
   *    |                       |
   *    |------------------------
   */
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
  @Test
  void deepDependencyGraphResolution() {
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
