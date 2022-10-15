package jedi.multiplesubtypes;

import jedi.Jedi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Produces;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

public class MultipleSubtypesTest {
    private Jedi cdi;

    @BeforeEach
    void setUp() {
        cdi = new Jedi("jedi.multiplesubtypes");
    }

    @Test
    void multipleSubtypesWithQualifier() {
        var instance = cdi.select(G.class);
        var g = instance.get();
        assertAll("All",
                () -> assertNotNull(g),
                () -> assertTrue(g instanceof AG),
                () -> assertNotNull(g.getD()),
                () -> assertEquals("hohoho", g.getStr())
        );
    }

    public static class StrFactory {
        @Produces
        public String getStr() {
            return "hohoho";
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
    @interface Async {
    }

    interface D {
    }

    interface G {
        D getD();

        String getStr();
    }

    public static class BD implements D {
    }

    @Async
    public static class CD implements D {
    }

    public static class AG implements G {
        D d;
        String str;

        public AG(@Async D d, String str) {
            this.d = d;
            this.str = str;
        }

        @Override
        public D getD() {
            return d;
        }

        @Override
        public String getStr() {
            return str;
        }
    }
}
