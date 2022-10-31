package jedi.resolution.circulardependency;

import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CircularDependencyTest {

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.circulardependency");
  }

  interface Z {}
  interface Y {}
  static class CZ implements Z {
    public CZ(Y y) {}
  }
  static class DY implements Y {
    public DY(Z z) {}
  }
  @Test
  void shallowCircularDependency() {
    var exception = assertThrows(JeDI.CircularDependencyException.class,
        () -> di.select(Z.class));
    assertTrue(exception.getMessage().contains(Z.class.getName()));
  }

  interface A {}
  interface B {}
  interface C {}
  interface D {}
  interface E {}
  interface F {}
  interface G {}

  static class HA implements A {
    public HA(F f, B b, C c) {}
  }

  static class IF implements F {
    public IF(D d, E e) {}
  }

  static class JD implements D {
    public JD(E e) {}
  }

  static class KE implements E {
    public KE(B b, C c) {}
  }

  static class LB implements B {}

  static class MC implements C {
    public MC(G g) {}
  }

  static class NG implements G {
    public NG(D d) {}
  }
  @Test
  void deepCircularDependency() {
    var exception = assertThrows(JeDI.CircularDependencyException.class,
        () -> di.select(A.class));
    assertTrue(exception.getMessage().contains(D.class.getName()));
  }

  @Nested
  class AmongConcreteTypesOnly {

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
