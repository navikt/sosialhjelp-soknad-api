package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfo;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo.dkif.dto.DigitalKontaktinfoBolk;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
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

    private DigitalKontaktinfoBolk createDigitalKontaktinfoBolk() {
        return new DigitalKontaktinfoBolk(
                new DigitalKontaktinfo(mobiltelefonnummer),
                null
        );
    }
}