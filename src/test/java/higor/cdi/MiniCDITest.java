package higor.cdi;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.scanners.Scanners;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;


class MiniCDITest {

    private CDI<Object> cdi;

    @BeforeEach
    void setUp() {
        cdi = new MiniCDI("higor.cdi", Scanners.SubTypes, Scanners.TypesAnnotated);
    }

//    @Test
    void shouldGetAClassInstanceByTheClassType() {
        ClassA a = ClassAFactory.getClassA();
//        MiniCDI cdi = new MiniCDI();
//        ClassA a = cdi.get(ClassA.class);
        assertNotNull(a);
    }

//        @Test
    void implementationAmbiguity() {
//        class B implements A {}
//        class C implements A {}
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

    @Test
    void theClassHasOnlyTheDefaultConstructor() {
        class A {}
        var a = cdi.select(A.class).get();
        assertNotNull(a);
    }

    @Test
    void aClassWithOnlyOneEmptyPublicConstructor() {
        class A {
            public A() {}
        }
        var a = cdi.select(A.class).get();
        assertNotNull(a);
    }

    @Test
    void aClassWithMultipleConstructorsThatCannotBeResolved() {
        class A {
            public A() {}
            public A(String str) {}
        }
        assertThrows(AmbiguousResolutionException.class, () -> cdi.select(A.class));
    }

    @Test
    void aClassWithMultipleConstructorsThatCanBeResolved_andWithAnUnresolvableDependency() {
        class A {
            public A() {}
            @Inject
            public A(String str) {}
        }
        assertThrows(AmbiguousResolutionException.class, () -> cdi.select(A.class));
    }

    @Test
    void aClassWithMultipleConstructorsThatCanBeResolved_andWithAnResolvableDependency() {
        class A {
            public A() {}
            @Inject
            public A(String str) {}
        }
        class StrFactory {
            @Produces
            public String getStr() {
                return "hohoho";
            }
        }
        var cdi = new MiniCDI("higor.cdi");
        var instance = cdi.select(A.class);
        assertNotNull(instance.get());
    }

    @Test
    void findAnInterfaceImplementation() {
        class B implements A {}
        A a = cdi.select(A.class).get();
        assertNotNull(a);
        assertTrue(a instanceof B);
    }

    @Test
    void multipleSubtypesWithoutQualifier() {
        class B implements D {
        }
        class C implements D {
        }
        var instance = cdi.select(D.class);
        assertTrue(instance.isAmbiguous());
    }

    @Test
    void findAnConcreteInstanceFromAnAbstract() {
        abstract class E {}
        class B extends E {}
        var e = cdi.select(E.class).get();
        assertNotNull(e);
        assertTrue(e instanceof B);
    }

    @Test
    void interfaceWithoutImplementation() {
        var instanceA = cdi.select(A.class);
        assertFalse(instanceA.isUnsatisfied());
        var instanceF = cdi.select(F.class);
        assertTrue(instanceF.isUnsatisfied());
    }

    interface A {}
    interface D {}
    interface F {}
}
