package higor.cdi;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

public class MiniCDIProvider implements CDIProvider {
    @Override
    public CDI<Object> getCDI() {
        return new MiniCDI("higor.cdi");
    }
}
