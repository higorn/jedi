package higor.cdi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalClassConstructorParamAnnotationBugTest {

    @Test
    void getAnnotationsFromConstructorParamOfInnerClass() {
        class A {
            public A(@Deprecated String str) {}
        }

        var constructor = A.class.getConstructors()[0];
        var parameters = constructor.getParameters();

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> parameters[1].getAnnotations());
    }
}
