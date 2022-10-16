package jedi.concretetyperesolution;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.scanners.Scanners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConcreteTypeResolutionTest {

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.concretetyperesolution", Scanners.SubTypes, Scanners.TypesAnnotated);
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
    assertThrows(AmbiguousResolutionException.class, () -> di.select(A.class));
  }
}
