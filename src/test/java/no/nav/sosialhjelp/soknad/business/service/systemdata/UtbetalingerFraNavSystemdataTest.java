package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.NavUtbetalingerService;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.Komponent;
import no.nav.sosialhjelp.soknad.inntekt.navutbetalinger.domain.NavUtbetaling;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.service.systemdata.UtbetalingerFraNavSystemdata.tilIntegerMedAvrunding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtbetalingerFraNavSystemdataTest {

    private static final String EIER = "12345678901";

    private static final JsonOkonomiOpplysningUtbetaling JSON_OKONOMI_OPPLYSNING_UTBETALING = new JsonOkonomiOpplysningUtbetaling()
            .withKilde(JsonKilde.BRUKER)
            .withType("Vaffelsalg")
            .withBelop(1000000);

    private static final LocalDate UTBETALINGSDATO = LocalDate.now().minusDays(5);
    private static final LocalDate PERIODE_FOM = LocalDate.now().minusDays(40);
    private static final LocalDate PERIODE_TOM = LocalDate.now().minusDays(10);

    private static final String TITTEL = "Onkel Skrue penger";
    private static final double NETTO = 60000.0;
    private static final double BRUTTO = 3880.0;
    private static final double SKATT = -1337.0;
    private static final double TREKK = -500.0;
    private static final String KOMPONENTTYPE = "Pengesekk";
    private static final double KOMPONENTBELOP = 50000.0;
    private static final String SATSTYPE = "Dag";
    private static final double SATSBELOP = 5000.0;
    private static final double SATSANTALL = 10.0;

    private static final String TITTEL_2 = "Lønnsinntekt";
    private static final double NETTO_2 = 10000.0;
    private static final double BRUTTO_2 = 12500.0;
    private static final double SKATT_2 = -2500.0;
    private static final double TREKK_2 = 0.0;
    private static final String ORGANISASJONSNR = "012345678";
    private static final String PERSONNR = "01010011111";

    private static final Komponent NAV_KOMPONENT = new Komponent(KOMPONENTTYPE, KOMPONENTBELOP, SATSTYPE, SATSBELOP, SATSANTALL);
    private static final NavUtbetaling NAV_UTBETALING_1 = new NavUtbetaling("type", NETTO, BRUTTO, SKATT, TREKK, null, UTBETALINGSDATO, PERIODE_FOM, PERIODE_TOM, singletonList(NAV_KOMPONENT), TITTEL, "orgnr");
    private static final NavUtbetaling NAV_UTBETALING_2 = new NavUtbetaling("type", NETTO_2, BRUTTO_2, SKATT_2, TREKK_2, null, UTBETALINGSDATO, PERIODE_FOM, PERIODE_TOM, emptyList(), TITTEL_2, ORGANISASJONSNR);
    private static final NavUtbetaling NAV_UTBETALING_3 = new NavUtbetaling("type", NETTO_2, BRUTTO_2, SKATT_2, TREKK_2, null, UTBETALINGSDATO, PERIODE_FOM, PERIODE_TOM, singletonList(NAV_KOMPONENT), TITTEL_2, PERSONNR);

    @Mock
    private OrganisasjonService organisasjonService;

    @Mock
    private NavUtbetalingerService navUtbetalingerService;

    @InjectMocks
    private UtbetalingerFraNavSystemdata utbetalingerFraNavSystemdata;

    @Test
    void skalOppdatereUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<NavUtbetaling> nav_utbetalinger = Arrays.asList(NAV_UTBETALING_1, NAV_UTBETALING_2);
        when(navUtbetalingerService.getUtbetalingerSiste40Dager(anyString())).thenReturn(nav_utbetalinger);

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_1, utbetaling, UTBETALING_NAVYTELSE);
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_2, utbetaling_1, UTBETALING_NAVYTELSE);
    }

    @Test
    void skalKunInkludereGyldigeOrganisasjonsnummer() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<NavUtbetaling> nav_utbetalinger = Arrays.asList(NAV_UTBETALING_1, NAV_UTBETALING_2, NAV_UTBETALING_3);
        when(navUtbetalingerService.getUtbetalingerSiste40Dager(anyString())).thenReturn(nav_utbetalinger);

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);
        JsonOkonomiOpplysningUtbetaling utbetaling_2 = jsonUtbetalinger.get(2);

        assertThat(jsonUtbetalinger).hasSize(3);
        assertThat(utbetaling.getOrganisasjon()).isNull();
        assertThat(utbetaling_1.getOrganisasjon().getOrganisasjonsnummer()).isEqualTo(ORGANISASJONSNR);
        assertThat(utbetaling_2.getOrganisasjon()).isNull();
    }

    @Test
    void skalIkksLasteNedUtbetalingerUtenSamtykke() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        List<NavUtbetaling> utbetalinger = singletonList(NAV_UTBETALING_1);
        when(navUtbetalingerService.getUtbetalingerSiste40Dager(anyString())).thenReturn(utbetalinger);

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        assertThat(utbetaling_1.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_1, utbetaling_1, UTBETALING_NAVYTELSE);
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUtbetalinger() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = new ArrayList<>();
        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().withUtbetaling(jsonUtbetalinger);
        return jsonInternalSoknad;
    }

    private void assertThatUtbetalingIsCorrectlyConverted(NavUtbetaling navUtbetaling, JsonOkonomiOpplysningUtbetaling jsonUtbetaling, String type) {
        assertThat(jsonUtbetaling.getType()).isEqualTo(type);
        assertThat(jsonUtbetaling.getTittel()).isEqualTo(navUtbetaling.getTittel());
        assertThat(jsonUtbetaling.getBelop()).isEqualTo(tilIntegerMedAvrunding(String.valueOf(navUtbetaling.getNetto())));
        assertThat(jsonUtbetaling.getBrutto()).isEqualTo(navUtbetaling.getBrutto());
        assertThat(jsonUtbetaling.getNetto()).isEqualTo(navUtbetaling.getNetto());
        assertThat(jsonUtbetaling.getUtbetalingsdato()).isEqualTo(navUtbetaling.getUtbetalingsdato().toString());
        assertThat(jsonUtbetaling.getPeriodeFom()).isEqualTo(navUtbetaling.getPeriodeFom().toString());
        assertThat(jsonUtbetaling.getPeriodeTom()).isEqualTo(navUtbetaling.getPeriodeTom().toString());
        assertThat(jsonUtbetaling.getSkattetrekk()).isEqualTo(navUtbetaling.getSkattetrekk());
        assertThat(jsonUtbetaling.getAndreTrekk()).isEqualTo(navUtbetaling.getAndreTrekk());
        assertThat(jsonUtbetaling.getOverstyrtAvBruker()).isFalse();
        if (!navUtbetaling.getKomponenter().isEmpty()) {
            for (int i = 0; i < navUtbetaling.getKomponenter().size(); i++) {
                Komponent komponent = navUtbetaling.getKomponenter().get(i);
                JsonOkonomiOpplysningUtbetalingKomponent jsonKomponent = jsonUtbetaling.getKomponenter().get(i);
                assertThat(jsonKomponent.getType()).isEqualTo(komponent.getType());
                assertThat(jsonKomponent.getBelop()).isEqualTo(komponent.getBelop());
                assertThat(jsonKomponent.getSatsType()).isEqualTo(komponent.getSatsType());
                assertThat(jsonKomponent.getSatsAntall()).isEqualTo(komponent.getSatsAntall());
                assertThat(jsonKomponent.getSatsBelop()).isEqualTo(komponent.getSatsBelop());
            }
        }
    }
}
