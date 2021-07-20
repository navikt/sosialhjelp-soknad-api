package no.nav.sosialhjelp.soknad.consumer;


import org.junit.jupiter.api.Test;

public class ServiceBuilderTest {
    @Test
    public void skalByggeTjeneste(){
        new ServiceBuilder<>(ServiceBuilderTestInterface.class)
                .asStandardService()
                .withTimeout()
                .withAddressing()
                .withLogging()
                .withProperties()
                .withExtraClasses(new Class[]{String.class})
                .build()
                .withMDC().get();
    }
}
