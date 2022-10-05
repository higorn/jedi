package higor.cdi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

class BeanInstanceTest {
    private Reflections reflections;

    @BeforeEach
    void setUp() {
        reflections = new Reflections("higor.cdi");
    }

    @Test
    void theClassHasOnlyTheDefaultConstructor() {
        class A {}
        var instance = new BeanInstance<>(A.class, reflections);
        var a = instance.get();
        assertNotNull(a);
    }

    @Test
    void aClassWithOnlyOneEmptyPublicConstructor() {
        class A {
            public A() {}
        }
        var instance = new BeanInstance<>(A.class, reflections);
        var a = instance.get();
        assertNotNull(a);
    }

    @Test
    void aClassWithMultipleConstructorsThatCannotBeResolved() {
        class A {
            public A() {}
            public A(String str) {}
        }
        assertThrows(AmbiguousResolutionException.class, () -> new BeanInstance<>(A.class, reflections));
    }

    @Test
    void aClassWithMultipleConstructorsThatCanBeResolved_andWithAnUnresolvableDependency() {
        class A {
            public A() {}
            @Inject
            public A(String str) {}
        }
        assertThrows(AmbiguousResolutionException.class, () -> new BeanInstance<>(A.class, reflections));
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
        reflections = new Reflections("higor.cdi", Scanners.values());
        var instance = new BeanInstance<>(A.class, reflections);
        assertNotNull(instance.get());
    }

    @Test
    void findAnInterfaceImplementation() {
        class B implements A {}
        var instance = new BeanInstance<>(A.class, reflections);
        A a = instance.get();
        assertNotNull(a);
        assertTrue(a instanceof B);
    }

    @Test
    void multipleSubtypesWithoutQualifier() {
        class B implements D {}
        class C implements D {}
        var instance = new BeanInstance<>(D.class, reflections);
        assertTrue(instance.isAmbiguous());
    }

    @Test
    void findAnAbstractConcreteInstance() {
        abstract class E {}
        class B extends E {}
        var instance = new BeanInstance<>(E.class, reflections);
        var e = instance.get();
        assertNotNull(e);
        assertTrue(e instanceof B);
    }

    @Test
    void interfaceWithoutImplementation() {
        var instanceA = new BeanInstance<>(A.class, reflections);
        assertFalse(instanceA.isUnsatisfied());
        var instanceF = new BeanInstance<>(F.class, reflections);
        assertTrue(instanceF.isUnsatisfied());
    }

    interface A {}
    interface D {}
    interface F {}
}
