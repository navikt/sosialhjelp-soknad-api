package no.nav.sosialhjelp.soknad.consumer.dkif;

import no.nav.sosialhjelp.soknad.consumer.dkif.dto.DigitalKontaktinfo;
import no.nav.sosialhjelp.soknad.consumer.dkif.dto.DigitalKontaktinfoBolk;
import no.nav.sosialhjelp.soknad.consumer.dkif.dto.Feil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DkifServiceTest {

    @Mock
    private DkifConsumer dkifConsumer;

    @InjectMocks
    private DkifService service;

    private String ident = "99988877777";
    private String mobiltelefonnummer = "12345678";

    @Test
    public void skalHenteMobiltelefonnummer() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(createDigitalKontaktinfoBolk());

        String response = service.hentMobiltelefonnummer(ident);

        assertThat(response).isEqualTo(mobiltelefonnummer);
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_ErNull() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(null);

        String response = service.hentMobiltelefonnummer(ident);

        assertThat(response).isNull();
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_Kontaktinfo_ErNull() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(new DigitalKontaktinfoBolk(null, null));

        String response = service.hentMobiltelefonnummer(ident);

        assertThat(response).isNull();
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_Kontaktinfo_Mobiltelefonnummer_ErNull() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(new DigitalKontaktinfoBolk(singletonMap(ident, new DigitalKontaktinfo(null)), null));

        String response = service.hentMobiltelefonnummer(ident);

        assertThat(response).isNull();
    }

    @Test
    public void skalReturnereNullHvis_DigitalKontaktinfoBolk_Feil_ErSatt() {
        when(dkifConsumer.hentDigitalKontaktinfo(anyString())).thenReturn(new DigitalKontaktinfoBolk(null, singletonMap(ident, new Feil("feil feil feil"))));

        String response = service.hentMobiltelefonnummer(ident);

        assertThat(response).isNull();
    }

    private DigitalKontaktinfoBolk createDigitalKontaktinfoBolk() {
        return new DigitalKontaktinfoBolk(
                singletonMap(ident, new DigitalKontaktinfo(mobiltelefonnummer)),
                null
        );
    }
}