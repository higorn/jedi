package jedi;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jedi.bean.BeanInstance;
import jedi.bean.ManagedBean;
import jedi.injection.ParameterInjectionPoint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BeanInstanceTest {

  @Test
  void aBeanWithNoDependencies() {
    class A {}
    Bean<A> bean = new ManagedBean<>(A.class, Set.of());
    Instance<A> instance = new BeanInstance<>(Set.of(bean));
    var a = instance.get();
    assertNotNull(a);
  }

  @Test
  void aBeanWithOneDependency() {
    class B {}
    class A {
      final B b;

      public A(B b) {
        this.b = b;
      }
    }
    var injectionPoint0 = new ParameterInjectionPoint(Set.of(), new ManagedBean<>(getClass(), Set.of()));
    var injectionPointB = new ParameterInjectionPoint(Set.of(), new ManagedBean<>(B.class, Set.of()));
    LinkedHashSet<InjectionPoint> injectionPoints = new LinkedHashSet<>();
    injectionPoints.add(injectionPoint0);
    injectionPoints.add(injectionPointB);
    Bean<A> bean = new ManagedBean<>(A.class, injectionPoints, (Constructor<A>) A.class.getConstructors()[0]);
    Instance<A> instance = new BeanInstance<>(Set.of(bean));
    var a = instance.get();

    assertNotNull(a);
    assertNotNull(a.b);
  }
}
