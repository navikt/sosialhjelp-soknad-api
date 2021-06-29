package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingerFraNavSystemdataOrganisasjonsnummerTest {

    @Mock
    OrganisasjonService organisasjonService;

    @InjectMocks
    private UtbetalingerFraNavSystemdata utbetalingerFraNavSystemdata;

    @Test
    public void skalReturnereOrganisasjonOmGyldigOrganisasjonsnummer() {
        String organisasjonsnummer = "089640782";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(organisasjonsnummer);
        assertThat(result).isNotNull();
        assertThat(result.getOrganisasjonsnummer()).isEqualTo(organisasjonsnummer);
    }

    @Test
    public void skalReturnereNullOmOrganisasjonsnummerInneholderTekst() {
        String organisasjonsnummer = "o89640782";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(organisasjonsnummer);
        assertThat(result).isNull();
    }

    @Test
    public void skalReturnereNullOmForKortOrganisasjonsnummer() {
        String nummer = "12345678";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(nummer);
        assertThat(result).isNull();
    }

    @Test
    public void skalReturnereNullOmForLangtOrganisasjonsnummer() {
        String nummer = "1234567890";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(nummer);
        assertThat(result).isNull();
    }

    @Test
    public void skalReturnereOrganisasjonUtenNummerVedPersonnummer() {
        String personnummer = "01010011111";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(personnummer);
        assertThat(result).isNull();
    }
}