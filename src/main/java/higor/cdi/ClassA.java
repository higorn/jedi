package higor.cdi;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

public class ClassA {

//    private final ClassD d;

/*
    public ClassA(ClassB b, String... s) {
        this.b = b;
    }
*/

    @Inject
//    public ClassA(ClassD d) {
    public ClassA(String d) {
//        this.d = d;
    }

//    public ClassB getB() {
//        return b;
//    }
}
