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
  private Jedi jedi;

  @BeforeEach
  void setUp() {
    jedi = new Jedi("jedi.multiplesubtypes");
  }

  @Test
  void multipleSubtypesWithQualifier() {
    var saberHandler = jedi.getBean(SaberHandler.class);

    assertNotNull(saberHandler);
    assertTrue(saberHandler instanceof MasterJedi);
    assertNotNull(saberHandler.getSaber());
    assertTrue(saberHandler.getSaber() instanceof LightSaber);
    assertEquals("May the bean be with you.", saberHandler.getMsg());
  }

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
  @interface Light {}

  interface Saber {}

  public static class CommonSaber implements Saber {}

  @Light
  public static class LightSaber implements Saber {}

  interface SaberHandler {
    Saber getSaber();
    String getMsg();
  }

  public static class MasterJedi implements SaberHandler {
    Saber  saber;
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

  public static class StringFactory {
    @Produces
    public String getMsg() {
      return "May the bean be with you.";
    }
  }
}