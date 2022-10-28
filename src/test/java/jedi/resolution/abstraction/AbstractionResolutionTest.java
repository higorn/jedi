package jedi.resolution.abstraction;

import jakarta.enterprise.inject.spi.CDI;
import jedi.JeDI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.scanners.Scanners;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractionResolutionTest {

  private CDI<Object> di;

  @BeforeEach
  void setUp() {
    di = new JeDI("jedi.resolution.abstraction", Scanners.SubTypes, Scanners.TypesAnnotated);
  }

  interface A {}
  public static class BA implements A {}
  @Test
  void findAnInterfaceImplementation() {
    A a = di.select(A.class).get();
    assertNotNull(a);
    assertTrue(a instanceof BA);
  }

  interface B {}
  interface C {}
  @Test
  void interfaceWithoutImplementation() {
    var instanceB = di.select(B.class);
    assertTrue(instanceB.isUnsatisfied());
    var instanceC = di.select(C.class);
    assertTrue(instanceC.isUnsatisfied());
  }

//  void aClassWithAnUnsatisfiedDependency

  abstract static class D {}
  public static class CD extends D {}
  @Test
  void findAnConcreteInstanceFromAnAbstract() {
    var d = di.select(D.class).get();
    assertNotNull(d);
    assertTrue(d instanceof CD);
  }
}