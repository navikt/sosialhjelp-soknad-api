package no.nav.sbl.dialogarena.rest;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoggTest {

    public static final String MELDING = "Melding";
    public static final String URL = "http://nav.no/url";

    @Test
    public void testMeldingOutput() {
        Logg logg = new Logg();
        logg.setMessage(MELDING);
        logg.setUrl(URL);

        Assertions.assertThat(logg.melding()).isEqualTo(MELDING + " at " + URL);
    }

}
