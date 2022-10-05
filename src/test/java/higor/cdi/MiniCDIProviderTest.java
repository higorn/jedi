package higor.cdi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MiniCDIProviderTest {

    @Test
    void shouldProvideACDI() {
        var cdi = new MiniCDIProvider().getCDI();
        assertNotNull(cdi);
    }
}
