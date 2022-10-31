package jedi.bean;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jedi.injection.ParameterInjectionPoint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BeanInstanceTest {

  public class A {}
  @Test
  void aBeanWithNoDependencies() {
    Bean<A> bean = new ManagedBean<>(A.class, Set.of());
    Instance<A> instance = new BeanInstance<>(Set.of(bean));
    var a = instance.get();
    assertNotNull(a);
  }

  public class B {}
  public class C {
    final B b;

    public C(B b) {
      this.b = b;
    }
  }
  @Test
  void aBeanWithOneDependency() {
    var injectionPoint0 = new ParameterInjectionPoint(Set.of(), new ManagedBean<>(getClass(), Set.of()));
    var injectionPointB = new ParameterInjectionPoint(Set.of(), new ManagedBean<>(B.class, Set.of()));
    LinkedHashSet<InjectionPoint> injectionPoints = new LinkedHashSet<>();
    injectionPoints.add(injectionPoint0);
    injectionPoints.add(injectionPointB);
    Bean<C> bean = new ManagedBean<>(C.class, injectionPoints, (Constructor<C>) C.class.getConstructors()[0]);
    Instance<C> instance = new BeanInstance<>(Set.of(bean));
    var c = instance.get();

    assertNotNull(c);
    assertNotNull(c.b);
  }
}
