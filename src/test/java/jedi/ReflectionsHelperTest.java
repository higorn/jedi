package jedi;

import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static jedi.ReflectionsHelper.getInjectableConstructor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReflectionsHelperTest {

  @Test
  void getTheMinimQualifiers() {
    @Alternative
    class A {}
    var qualifiers = ReflectionsHelper.getQualifiers(A.class);
    assertEquals(2, qualifiers.size());
  }

  @Test
  void getTheMinimQualifiersEvenIfTheyWereExplicitlyDeclared() {
    @Any
    @Default
    @Alternative
    class A {}
    var qualifiers = ReflectionsHelper.getQualifiers(A.class);
    assertEquals(2, qualifiers.size());
  }

  @Nested
  class GetInjectableConstructor {

    class B {
      public B(String str) {}
    }
    @Test
    void aNonPublicClassWithAnExplicitPublicConstructor() {
      var constructor = getInjectableConstructor(B.class);
      assertNotNull(constructor);
    }

    class C {}
    @Test
    void aNonPublicClassWithNoDeclaredConstructor() {
      var constructor = getInjectableConstructor(C.class);
      assertNotNull(constructor);
    }
  }
}