package jedi;

import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}