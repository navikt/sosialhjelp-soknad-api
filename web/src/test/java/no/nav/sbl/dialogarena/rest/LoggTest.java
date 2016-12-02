package no.nav.sbl.dialogarena.rest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggTest {

    @Test
    public void testMeldingOutput() {
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
