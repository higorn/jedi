# Jedi
The Java Easy Dependency Injection (JEDI) framework is a humble implementation of the DI part of the [jakarta CDI 4.0 
specification](https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html).
This is a work in progress project with the intention to cover only the DI aspect of the the above mentioned jakarta 
spec.
The main goal is to create a lightweight and easy DI framework focused in the small projects needs.

# Example
```java
package example;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@interface Light {}

interface Saber {}

class CommonSaber implements Saber {}

@Light
class LightSaber implements Saber {}

interface SaberHandler {
  Saber getSaber();
  String getMsg();
}

class MasterJedi implements SaberHandler {
  Saber saber;
  String msg;

  public MasterJedi(@Light Saber saber, String msg) {
    this.saber = saber;
    this.msg = msg;
  }

  @Override
  public Saber getSaber() {
    return saber;
  }

  @Override
  public String getMsg() {
    return msg;
  }
}

class StringFactory {
  @Produces
  public String getMsg() {
    return "May the bean be with you.";
  }
}

class Main {
  
  public static void main(String... args) {
    var di = new JeDI("example");
    SaberHandler saberHandler = di.getBean(SaberHandler.class);

    assertTrue(saberHandler instanceof MasterJedi);
    assertNotNull(saberHandler.getSaber());
    assertTrue(saberHandler.getSaber() instanceof LightSaber);
    assertEquals("May the bean be with you", saberHandler.getMsg());
  }
}
```

## More examples soon...