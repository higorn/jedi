package jedi;

import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;

import static org.junit.jupiter.api.Assertions.*;

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