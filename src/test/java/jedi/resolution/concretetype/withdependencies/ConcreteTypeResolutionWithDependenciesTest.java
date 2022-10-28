package jedi.resolution.concretetype.withdependencies;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reflections.scanners.Scanners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConcreteTypeResolutionWithDependenciesTest {

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.concretetype.withdependencies", Scanners.SubTypes, Scanners.TypesAnnotated);
  }

  @Test
  void aClassWithMultipleConstructorsThatCanBeResolved_andWithAnUnresolvableDependency() {
    class A {
      public A() {}
      @Inject
      public A(String str) {}
    }
    assertThrows(AmbiguousResolutionException.class, () -> di.select(A.class));
  }

  public static class B {
    public B() {}
    @Inject
    public B(String str) {}
  }
  static public class StrFactory {
    @Produces
    public String getStr() {
      return "hohoho";
    }
  }
  @Test
  void aClassWithMultipleConstructorsThatCanBeResolved_andWithAResolvableDependency() {
    var cdi = new JeDI("jedi");
    var instance = cdi.select(B.class);
    assertNotNull(instance.get());
  }

  class CircA {
    public CircA(CircB b) {}
  }
  class CircB {
    public CircB(CircA a) {}
  }
  @Test
  void shallowCircularDependency() {
    assertThrows(JeDI.CircularDependencyException.class, () -> di.select(CircA.class));
  }

  @Nested
  class DeepCircularDependencyTest{

    /*
     * A ----> B ----> D
     * |             / ^
     * v            /  |
     * C <---------/   |
     * |               |
     * v               |
     * E --------------|
     */
    class A {
      public A(B b, C c) {}
    }
    class B {
      public B(D d) {}
    }
    class C {
      public C(E e) {}
    }
    class D {
      public D(C c) {}
    }
    class E {
      public E(D d) {}
    }
    @Test
    void case1() {
      assertThrows(JeDI.CircularDependencyException.class, () -> di.select(A.class));
    }

    /*
     * A ----> B ----> D
     * |       ^     /
     * v       |    /
     * C <---------/
     * |       |
     * v       |
     * E -------
     */

    class A2 {
      public A2(B2 b2, C2 c2) {}
    }
    class B2 {
      public B2(D2 d2) {}
    }
    class C2 {
      public C2(E2 e2) {}
    }
    class D2 {
      public D2(C2 c2) {}
    }
    class E2 {
      public E2(B2 b) {}
    }
    @Test
    void case2() {
      assertThrows(JeDI.CircularDependencyException.class, () -> di.select(A2.class));
    }
  }
}
