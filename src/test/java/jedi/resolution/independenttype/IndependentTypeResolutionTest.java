package jedi.resolution.independenttype;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.scanners.Scanners;

import static org.junit.jupiter.api.Assertions.*;

public class IndependentTypeResolutionTest {

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.independenttype", Scanners.SubTypes, Scanners.TypesAnnotated);
  }

  public static class A {}
  @Test
  void theClassHasOnlyTheDefaultConstructor() {
    var a = di.select(A.class).get();
    assertNotNull(a);
  }

  public static class B {
    public B() {}
  }
  @Test
  void aClassWithOnlyOneEmptyPublicConstructor() {
    var a = di.select(B.class).get();
    assertNotNull(a);
  }

  @Test
  void aClassWithMultipleConstructorsThatCannotBeResolved() {
    class A {
      public A() {}

      public A(String str) {}
    }
    var exception = assertThrows(AmbiguousResolutionException.class, () -> di.select(A.class));
    assertTrue(exception.getMessage().contains("Ambiguous constructors"));
  }

  interface C {}
  public static class BC implements C {}
  @Test
  void findAnInterfaceImplementation() {
    var c = di.select(C.class).get();
    assertNotNull(c);
    assertTrue(c instanceof BC);
  }

  interface D {}
  interface E {}
  @Test
  void interfaceWithoutImplementation() {
    var instanceD = di.select(D.class);
    assertTrue(instanceD.isUnsatisfied());
    var instanceE = di.select(E.class);
    assertTrue(instanceE.isUnsatisfied());
  }

  abstract static class F {}
  public static class CF extends F {}
  @Test
  void findAnConcreteInstanceFromAnAbstract() {
    var f = di.select(F.class).get();
    assertNotNull(f);
    assertTrue(f instanceof CF);
  }
}
