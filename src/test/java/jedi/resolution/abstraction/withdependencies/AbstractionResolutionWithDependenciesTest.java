package jedi.resolution.abstraction.withdependencies;

import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractionResolutionWithDependenciesTest {

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.abstraction.withdependencies");
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
}
