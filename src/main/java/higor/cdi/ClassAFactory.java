package higor.cdi;

import org.jboss.weld.SimpleCDI;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.inject.Instance;

public class ClassAFactory {
    public static ClassA getClassA() {
        System.setProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, "true");
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        Instance<ClassA> instance = container.select(ClassA.class);
        return instance.get();
    }
}
