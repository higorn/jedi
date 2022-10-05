package higor.cdi;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;


class MiniCDITest {

    private CDI<Object> cdi;

    @BeforeEach
    void setUp() {
        cdi = CDI.current();
    }

//    @Test
    void shouldGetAClassInstanceByTheClassType() {
        ClassA a = ClassAFactory.getClassA();
//        MiniCDI cdi = new MiniCDI();
//        ClassA a = cdi.get(ClassA.class);
        assertNotNull(a);
    }

        @Test
    void implementationAmbiguity() {
        class B implements A {}
        class C implements A {}
        class D {
            @Inject
            private A a;
        }
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        Instance<ClassA> instance = container.select(ClassA.class);
//        var instance = container.select(D.class);
        var d = instance.get();
//        A a = cdi.select(A.class).get();
        assertNotNull(d);
    }
    interface A {}
}
