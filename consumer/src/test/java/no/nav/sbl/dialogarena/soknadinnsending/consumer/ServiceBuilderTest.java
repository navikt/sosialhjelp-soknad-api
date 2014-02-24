package no.nav.sbl.dialogarena.soknadinnsending.consumer;


import org.junit.Test;

public class ServiceBuilderTest {
    @Test
    public void skalByggeTjeneste(){
        new ServiceBuilder<>(ServiceBuilderTest.class)
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
