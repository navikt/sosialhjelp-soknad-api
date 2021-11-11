package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UtbetalingerFraNavSystemdataOrganisasjonsnummerTest {

    @Mock
    private OrganisasjonService organisasjonService;

    @InjectMocks
    private UtbetalingerFraNavSystemdata utbetalingerFraNavSystemdata;

    @Test
    void skalReturnereOrganisasjonOmGyldigOrganisasjonsnummer() {
        String organisasjonsnummer = "089640782";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(organisasjonsnummer);
        assertThat(result).isNotNull();
        assertThat(result.getOrganisasjonsnummer()).isEqualTo(organisasjonsnummer);
    }

    @Test
    void skalReturnereNullOmOrganisasjonsnummerInneholderTekst() {
        String organisasjonsnummer = "o89640782";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(organisasjonsnummer);
        assertThat(result).isNull();
    }

    @Test
    void skalReturnereNullOmForKortOrganisasjonsnummer() {
        String nummer = "12345678";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(nummer);
        assertThat(result).isNull();
    }

    @Test
    void skalReturnereNullOmForLangtOrganisasjonsnummer() {
        String nummer = "1234567890";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(nummer);
        assertThat(result).isNull();
    }

    @Test
    void skalReturnereOrganisasjonUtenNummerVedPersonnummer() {
        String personnummer = "01010011111";
        JsonOrganisasjon result = utbetalingerFraNavSystemdata.mapToJsonOrganisasjon(personnummer);
        assertThat(result).isNull();
    }
}