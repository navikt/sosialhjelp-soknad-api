package no.nav.sosialhjelp.soknad.web.rest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggTest {

    @Test
    void testMeldingOutput() {
        Logg logg = new Logg();
        String feilmelding = "Cannot read blabla of undefined";
        logg.setMessage(feilmelding);
        logg.setUrl("http://nav.no/url");
        logg.setJsFileUrl("minFil.js");
        logg.setLineNumber("100");
        logg.setColumnNumber("99");
        logg.setUserAgent("IE ROCKS,MSIE");

        assertThat(logg.melding()).isEqualTo("jsmessagehash="+feilmelding.hashCode()+", fileUrl=minFil.js:100:99, url=http://nav.no/url, userAgent=IE_ROCKS_MSIE, melding: Cannot read blabla of undefined");
    }

}
