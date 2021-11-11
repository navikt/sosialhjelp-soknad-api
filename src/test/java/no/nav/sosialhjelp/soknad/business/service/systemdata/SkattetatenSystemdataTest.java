package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.service.systemdata.UtbetalingerFraNavSystemdata.tilIntegerMedAvrunding;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkattetatenSystemdataTest {
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

    private static final String TITTEL_2 = "Lønnsinntekt";
    private static final double NETTO_2 = 10000.0;
    private static final double BRUTTO_2 = 12500.0;
    private static final double SKATT_2 = -2500.0;
    private static final double TREKK_2 = 0.0;
    private static final String KOMPONENTTYPE_2 = "Månedslønn";
    private static final double KOMPONENTBELOP_2 = 10000.0;
    private static final String SATSTYPE_2 = "Årslønn";
    private static final double SATSBELOP_2 = 120000.0;
    private static final double SATSANTALL_2 = 12.0;
    private static final String ORGANISASJONSNR = "012345678";
    private static final String PERSONNR = "01010011111";

    private static final Utbetaling SKATTBAR_UTBETALING_ANNEN = new Utbetaling();
    private static final Utbetaling SKATTBAR_UTBETALING = new Utbetaling();
    private static final Utbetaling SKATTBAR_UTBETALING_FRA_PRIVATPERSON = new Utbetaling();
    private static final Utbetaling.Komponent SKATTBAR_KOMPONENT = new Utbetaling.Komponent();

    static {
        SKATTBAR_UTBETALING.tittel = TITTEL_2;
        SKATTBAR_UTBETALING.netto = NETTO_2;
        SKATTBAR_UTBETALING.brutto = BRUTTO_2;
        SKATTBAR_UTBETALING.skattetrekk = SKATT_2;
        SKATTBAR_UTBETALING.andreTrekk = TREKK_2;
        SKATTBAR_UTBETALING.utbetalingsdato = UTBETALINGSDATO;
        SKATTBAR_UTBETALING.periodeFom = PERIODE_FOM;
        SKATTBAR_UTBETALING.periodeTom = PERIODE_TOM;
        SKATTBAR_UTBETALING.orgnummer = ORGANISASJONSNR;

        SKATTBAR_UTBETALING_ANNEN.tittel = TITTEL;
        SKATTBAR_UTBETALING_ANNEN.netto = NETTO;
        SKATTBAR_UTBETALING_ANNEN.brutto = BRUTTO;
        SKATTBAR_UTBETALING_ANNEN.skattetrekk = SKATT;
        SKATTBAR_UTBETALING_ANNEN.andreTrekk = TREKK;
        SKATTBAR_UTBETALING_ANNEN.utbetalingsdato = UTBETALINGSDATO;
        SKATTBAR_UTBETALING_ANNEN.periodeFom = PERIODE_FOM;
        SKATTBAR_UTBETALING_ANNEN.periodeTom = PERIODE_TOM;

        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.tittel = TITTEL_2;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.netto = NETTO_2;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.brutto = BRUTTO_2;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.skattetrekk = SKATT_2;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.andreTrekk = TREKK_2;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.utbetalingsdato = UTBETALINGSDATO;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.periodeFom = PERIODE_FOM;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.periodeTom = PERIODE_TOM;
        SKATTBAR_UTBETALING_FRA_PRIVATPERSON.orgnummer = PERSONNR;

        SKATTBAR_KOMPONENT.type = KOMPONENTTYPE_2;
        SKATTBAR_KOMPONENT.belop = KOMPONENTBELOP_2;
        SKATTBAR_KOMPONENT.satsType = SATSTYPE_2;
        SKATTBAR_KOMPONENT.satsBelop = SATSBELOP_2;
        SKATTBAR_KOMPONENT.satsAntall = SATSANTALL_2;

        SKATTBAR_UTBETALING.komponenter = Collections.singletonList(SKATTBAR_KOMPONENT);
        SKATTBAR_UTBETALING_ANNEN.komponenter = Collections.singletonList(SKATTBAR_KOMPONENT);
    }

    @Mock
    private OrganisasjonService organisasjonService;

    @Mock
    private SkattbarInntektService skattbarInntektService;

    @InjectMocks
    private SkattetatenSystemdata skattetatenSystemdata;

    @Test
    void skalOppdatereUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> skattbare_utbetalinger = Arrays.asList(SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_ANNEN);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(skattbare_utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING, utbetaling, UTBETALING_SKATTEETATEN);
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling_1, UTBETALING_SKATTEETATEN);
    }

    @Test
    void skalKunInkludereGyldigeOrganisasjonsnummer() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> skattbare_utbetalinger = Arrays.asList(SKATTBAR_UTBETALING_ANNEN, SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_FRA_PRIVATPERSON);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(skattbare_utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);

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
    void skalOppdatereUtbetalingerUtenAAOverskriveBrukerUtfylteUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING_ANNEN);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        assertThat(utbetaling_1.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling_1, UTBETALING_SKATTEETATEN);
    }

    @Test
    void skalIkkeHenteUtbetalingerUtenSamtykke() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), false);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);

        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        assertThat(jsonUtbetalinger).hasSize(1);
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet()).isFalse();
    }

    @Test
    void skalFjerneUtbetalingerNarViIkkeHarSamtykke() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING_ANNEN);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalingerA = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetalingA = jsonUtbetalingerA.get(0);
        JsonOkonomiOpplysningUtbetaling utbetalingA_1 = jsonUtbetalingerA.get(1);

        //SJEKK STATE FOR TEST:
        assertThat(utbetalingA.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetalingA).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        assertThat(utbetalingA_1.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetalingA_1, UTBETALING_SKATTEETATEN);

        //TEST:
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), false);
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalingerB = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetalingB = jsonUtbetalingerB.get(0);

        assertThat(utbetalingB.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetalingB).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        assertThat(jsonUtbetalingerB).hasSize(1);
    }

    @Test
    void updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil_ogBeholderGamleData() {
        // Variabler:
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad()
                .getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        utbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);

        // Mock:
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(null);

        // Kjøring:
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);

        assertThat(utbetalinger).hasSize(1);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
        assertThat(utbetaling.getKilde()).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.getKilde());
        assertThat(utbetaling.getType()).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.getType());
        assertThat(utbetaling.getBelop()).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.getBelop());
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet()).isTrue();
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUtbetalinger() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = new ArrayList<>();
        setSamtykke(jsonInternalSoknad, true);
        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().withUtbetaling(jsonUtbetalinger);
        return jsonInternalSoknad;
    }

    private void setSamtykke(JsonInternalSoknad jsonInternalSoknad, boolean harSamtykke) {
        List<JsonOkonomibekreftelse> bekreftelser = jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
        bekreftelser.removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN_SAMTYKKE));
        bekreftelser
                .add(new JsonOkonomibekreftelse().withKilde(JsonKilde.SYSTEM)
                        .withType(UTBETALING_SKATTEETATEN_SAMTYKKE)
                        .withVerdi(harSamtykke)
                        .withTittel("beskrivelse"));
    }

    private void assertThatUtbetalingIsCorrectlyConverted(Utbetaling utbetaling, JsonOkonomiOpplysningUtbetaling jsonUtbetaling, String type) {
        assertThat(jsonUtbetaling.getType()).isEqualTo(type);
        assertThat(jsonUtbetaling.getTittel()).isEqualTo(utbetaling.tittel);
        assertThat(jsonUtbetaling.getBelop()).isEqualTo(tilIntegerMedAvrunding(String.valueOf(utbetaling.netto)));
        assertThat(jsonUtbetaling.getBrutto()).isEqualTo(utbetaling.brutto);
        assertThat(jsonUtbetaling.getNetto()).isEqualTo(utbetaling.netto);
        assertThat(jsonUtbetaling.getUtbetalingsdato()).isEqualTo(utbetaling.utbetalingsdato.toString());
        assertThat(jsonUtbetaling.getPeriodeFom()).isEqualTo(utbetaling.periodeFom.toString());
        assertThat(jsonUtbetaling.getPeriodeTom()).isEqualTo(utbetaling.periodeTom.toString());
        assertThat(jsonUtbetaling.getSkattetrekk()).isEqualTo(utbetaling.skattetrekk);
        assertThat(jsonUtbetaling.getAndreTrekk()).isEqualTo(utbetaling.andreTrekk);
        assertThat(jsonUtbetaling.getOverstyrtAvBruker()).isFalse();
        if (!utbetaling.komponenter.isEmpty()) {
            for (int i = 0; i < utbetaling.komponenter.size(); i++) {
                Utbetaling.Komponent komponent = utbetaling.komponenter.get(i);
                JsonOkonomiOpplysningUtbetalingKomponent jsonKomponent = jsonUtbetaling.getKomponenter().get(i);
                assertThat(jsonKomponent.getType()).isEqualTo(komponent.type);
                assertThat(jsonKomponent.getBelop()).isEqualTo(komponent.belop);
                assertThat(jsonKomponent.getSatsType()).isEqualTo(komponent.satsType);
                assertThat(jsonKomponent.getSatsAntall()).isEqualTo(komponent.satsAntall);
                assertThat(jsonKomponent.getSatsBelop()).isEqualTo(komponent.satsBelop);
            }
        }
    }
}
