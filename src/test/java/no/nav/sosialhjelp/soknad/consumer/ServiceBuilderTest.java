package no.nav.sosialhjelp.soknad.consumer;


import org.junit.jupiter.api.Test;

class ServiceBuilderTest {
    @Test
    void skalByggeTjeneste(){
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
