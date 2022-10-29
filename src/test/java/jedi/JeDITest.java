package jedi;

import jakarta.enterprise.inject.spi.CDI;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.BeforeEach;
import org.reflections.scanners.Scanners;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JeDITest {

  private CDI<Object> cdi;

  @BeforeEach
  void setUp() {
    cdi = new JeDI("jedi", Scanners.SubTypes, Scanners.TypesAnnotated);
  }

  //    @Test
  void shouldGetAClassInstanceByTheClassType() {
    ClassA a = ClassAFactory.getClassA();
    //        MiniCDI cdi = new MiniCDI();
    //        ClassA a = cdi.get(ClassA.class);
    assertNotNull(a);
  }

//          @Test
  void implementationAmbiguity() {
    //        class B implements A {}
    //        class C implements A {}
    Weld weld = new Weld();
    WeldContainer container = weld.initialize();
    //        var instance = container.select(ClassA.class);
    var instance = container.select(ClassA.class);
    var d = instance.get();
    //        A a = cdi.select(A.class).get();
    //        assertNotNull(d);
  }
}