package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfo;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfoBolk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.Feil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DkifServiceTest {

    @Mock
    private DkifConsumer dkifConsumer;

    @InjectMocks
    private DkifService service;

    private String mobiltelefonnummer = "12345678";

    @Before
    public void setUp() {
        System.setProperty("dkif_api_enabled", "true");
    }

    @Test
    public void skalHenteMobiltelefonnummer() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(createDigitalKontaktinfoBolk());

        String response = service.hentMobiltelefonnummer("ident");

        assertEquals(mobiltelefonnummer, response);
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_ErNull() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(null);

        String response = service.hentMobiltelefonnummer("ident");

        assertNull(response);
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_Kontaktinfo_ErNull() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(new DigitalKontaktinfoBolk(null, null));

        String response = service.hentMobiltelefonnummer("ident");

        assertNull(response);
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_Kontaktinfo_Mobiltelefonnummer_ErNull() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(new DigitalKontaktinfoBolk(new DigitalKontaktinfo(null), null));

        String response = service.hentMobiltelefonnummer("ident");

        assertNull(response);
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_Feil_ErSatt() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(new DigitalKontaktinfoBolk(null, new Feil("feil feil feil")));

        String response = service.hentMobiltelefonnummer("ident");

        assertNull(response);
    }

    private DigitalKontaktinfoBolk createDigitalKontaktinfoBolk() {
        return new DigitalKontaktinfoBolk(
                new DigitalKontaktinfo(mobiltelefonnummer),
                null
        );
    }
}