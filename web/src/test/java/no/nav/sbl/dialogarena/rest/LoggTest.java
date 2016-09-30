package no.nav.sbl.dialogarena.rest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggTest {

    @Test
    public void testMeldingOutput() {
        Logg logg = new Logg();
        logg.setMessage("Cannot read blabla of undefined");
        logg.setUrl("http://nav.no/url");
        logg.setJsFileUrl("minFil.js");
        logg.setLineNumber("100");
        logg.setColumnNumber("99");
        logg.setUserAgent("IE ROCKS");

        assertThat(logg.melding()).isEqualTo("Cannot read blabla of undefined i fil minFil.js:100:99 fra URL http://nav.no/url for userAgent IE ROCKS");
    }

}
