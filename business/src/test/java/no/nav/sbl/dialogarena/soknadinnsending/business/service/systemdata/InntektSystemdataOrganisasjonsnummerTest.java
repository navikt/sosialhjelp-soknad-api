package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InntektSystemdataOrganisasjonsnummerTest {

    @Mock
    private UtbetalingService utbetalingService;

    @Mock
    ArbeidsforholdTransformer arbeidsforholdTransformer;

    @Mock
    SkattbarInntektService skattbarInntektService;

    @InjectMocks
    private InntektSystemdata inntektSystemdata;


    @Test
    public void skalReturnereOrganisasjonOmGyldigOrganisasjonsnummer() {
        String organisasjonsnummer = "089640782";
        JsonOrganisasjon result = inntektSystemdata.mapToJsonOrganisasjon(organisasjonsnummer);
        assertNotNull(result);
        assertEquals(organisasjonsnummer, result.getOrganisasjonsnummer());
    }

    @Test
    public void skalReturnereNullOmOrganisasjonsnummerInneholderTekst() {
        String organisasjonsnummer = "o89640782";
        JsonOrganisasjon result = inntektSystemdata.mapToJsonOrganisasjon(organisasjonsnummer);
        assertNull(result);
    }

    @Test
    public void skalReturnereNullOmForKortOrganisasjonsnummer() {
        String nummer = "12345678";
        JsonOrganisasjon result = inntektSystemdata.mapToJsonOrganisasjon(nummer);
        assertNull(result);
    }

    @Test
    public void skalReturnereNullOmForLangtOrganisasjonsnummer() {
        String nummer = "1234567890";
        JsonOrganisasjon result = inntektSystemdata.mapToJsonOrganisasjon(nummer);
        assertNull(result);
    }

    @Test
    public void skalReturnereOrganisasjonUtenNummerVedPersonnummer() {
        String personnummer = "01010011111";
        JsonOrganisasjon result = inntektSystemdata.mapToJsonOrganisasjon(personnummer);
        assertNull(result);
    }
}