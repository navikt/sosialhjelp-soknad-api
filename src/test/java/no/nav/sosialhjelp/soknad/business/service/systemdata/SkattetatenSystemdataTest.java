//package no.nav.sosialhjelp.soknad.business.service.systemdata;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
//import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService;
//import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling;
//import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
//import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
//import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//@MockitoSettings(strictness = Strictness.LENIENT)
//class SkattetatenSystemdataTest {
//    private static final String EIER = "12345678901";
//
//    private static final JsonOkonomiOpplysningUtbetaling JSON_OKONOMI_OPPLYSNING_UTBETALING = new JsonOkonomiOpplysningUtbetaling()
//            .withKilde(JsonKilde.BRUKER)
//            .withType("Vaffelsalg")
//            .withBelop(1000000);
//
//    private static final LocalDate PERIODE_FOM = LocalDate.now().minusDays(40);
//    private static final LocalDate PERIODE_TOM = LocalDate.now().minusDays(10);
//
//    private static final String TITTEL = "Onkel Skrue penger";
//    private static final double BRUTTO = 3880.0;
//    private static final double SKATT = -1337.0;
//
//    private static final String TITTEL_2 = "Lønnsinntekt";
//    private static final double BRUTTO_2 = 12500.0;
//    private static final double SKATT_2 = -2500.0;
//    private static final String ORGANISASJONSNR = "012345678";
//    private static final String ORGANISASJONSNR_ANNEN = "999888777";
//    private static final String PERSONNR = "01010011111";
//
//    private static final Utbetaling SKATTBAR_UTBETALING_ANNEN = new Utbetaling("skatteopplysninger", BRUTTO, SKATT, PERIODE_FOM, PERIODE_TOM, TITTEL, ORGANISASJONSNR_ANNEN);
//    private static final Utbetaling SKATTBAR_UTBETALING = new Utbetaling("skatteopplysninger", BRUTTO_2, SKATT_2, PERIODE_FOM, PERIODE_TOM, TITTEL_2, ORGANISASJONSNR);
//    private static final Utbetaling SKATTBAR_UTBETALING_FRA_PRIVATPERSON = new Utbetaling("skatteopplysninger", BRUTTO_2, SKATT_2, PERIODE_FOM, PERIODE_TOM, TITTEL_2, PERSONNR);
//
//    @Mock
//    private OrganisasjonService organisasjonService;
//
//    @Mock
//    private SkattbarInntektService skattbarInntektService;
//
//    @InjectMocks
//    private SkattetatenSystemdata skattetatenSystemdata;
//
//    @Test
//    void skalOppdatereUtbetalinger() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
//                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
//        List<Utbetaling> skattbare_utbetalinger = Arrays.asList(SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_ANNEN);
//        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(skattbare_utbetalinger);
//
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
//        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);
//
//        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.SYSTEM);
//        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING, utbetaling, UTBETALING_SKATTEETATEN);
//        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling_1, UTBETALING_SKATTEETATEN);
//    }
//
//    @Test
//    void skalKunInkludereGyldigeOrganisasjonsnummer() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
//                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
//        List<Utbetaling> skattbare_utbetalinger = Arrays.asList(SKATTBAR_UTBETALING_ANNEN, SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_FRA_PRIVATPERSON);
//        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(skattbare_utbetalinger);
//
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
//        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);
//        JsonOkonomiOpplysningUtbetaling utbetaling_2 = jsonUtbetalinger.get(2);
//
//        assertThat(jsonUtbetalinger).hasSize(3);
//        assertThat(utbetaling.getOrganisasjon().getOrganisasjonsnummer()).isEqualTo(ORGANISASJONSNR_ANNEN);
//        assertThat(utbetaling_1.getOrganisasjon().getOrganisasjonsnummer()).isEqualTo(ORGANISASJONSNR);
//        assertThat(utbetaling_2.getOrganisasjon()).isNull();
//    }
//
//    @Test
//    void skalOppdatereUtbetalingerUtenAAOverskriveBrukerUtfylteUtbetalinger() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
//                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
//        List<Utbetaling> utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING_ANNEN);
//        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(utbetalinger);
//
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
//        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);
//
//        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
//        assertThat(utbetaling_1.getKilde()).isEqualTo(JsonKilde.SYSTEM);
//        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling_1, UTBETALING_SKATTEETATEN);
//    }
//
//    @Test
//    void skalIkkeHenteUtbetalingerUtenSamtykke() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
//                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), false);
//
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
//
//        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(utbetaling).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
//        assertThat(jsonUtbetalinger).hasSize(1);
//        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet()).isFalse();
//    }
//
//    @Test
//    void skalFjerneUtbetalingerNarViIkkeHarSamtykke() {
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
//                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
//        List<Utbetaling> utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING_ANNEN);
//        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(utbetalinger);
//
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalingerA = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        JsonOkonomiOpplysningUtbetaling utbetalingA = jsonUtbetalingerA.get(0);
//        JsonOkonomiOpplysningUtbetaling utbetalingA_1 = jsonUtbetalingerA.get(1);
//
//        //SJEKK STATE FOR TEST:
//        assertThat(utbetalingA.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(utbetalingA).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
//        assertThat(utbetalingA_1.getKilde()).isEqualTo(JsonKilde.SYSTEM);
//        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetalingA_1, UTBETALING_SKATTEETATEN);
//
//        //TEST:
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), false);
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalingerB = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        JsonOkonomiOpplysningUtbetaling utbetalingB = jsonUtbetalingerB.get(0);
//
//        assertThat(utbetalingB.getKilde()).isEqualTo(JsonKilde.BRUKER);
//        assertThat(utbetalingB).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING);
//        assertThat(jsonUtbetalingerB).hasSize(1);
//    }
//
//    @Test
//    void updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil_ogBeholderGamleData() {
//        // Variabler:
//        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
//                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
//        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
//        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad()
//                .getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
//        utbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);
//
//        // Mock:
//        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(null);
//
//        // Kjøring:
//        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
//
//        assertThat(utbetalinger).hasSize(1);
//        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
//        assertThat(utbetaling.getKilde()).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.getKilde());
//        assertThat(utbetaling.getType()).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.getType());
//        assertThat(utbetaling.getBelop()).isEqualTo(JSON_OKONOMI_OPPLYSNING_UTBETALING.getBelop());
//        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet()).isTrue();
//    }
//
//    private JsonInternalSoknad createJsonInternalSoknadWithUtbetalinger() {
//        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
//        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = new ArrayList<>();
//        setSamtykke(jsonInternalSoknad, true);
//        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);
//        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().withUtbetaling(jsonUtbetalinger);
//        return jsonInternalSoknad;
//    }
//
//    private void setSamtykke(JsonInternalSoknad jsonInternalSoknad, boolean harSamtykke) {
//        List<JsonOkonomibekreftelse> bekreftelser = jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
//        bekreftelser.removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN_SAMTYKKE));
//        bekreftelser
//                .add(new JsonOkonomibekreftelse().withKilde(JsonKilde.SYSTEM)
//                        .withType(UTBETALING_SKATTEETATEN_SAMTYKKE)
//                        .withVerdi(harSamtykke)
//                        .withTittel("beskrivelse"));
//    }
//
//    private void assertThatUtbetalingIsCorrectlyConverted(Utbetaling utbetaling, JsonOkonomiOpplysningUtbetaling jsonUtbetaling, String type) {
//        assertThat(jsonUtbetaling.getType()).isEqualTo(type);
//        assertThat(jsonUtbetaling.getTittel()).isEqualTo(utbetaling.getTittel());
//        assertThat(jsonUtbetaling.getBelop()).isNull();
//        assertThat(jsonUtbetaling.getBrutto()).isEqualTo(utbetaling.getBrutto());
//        assertThat(jsonUtbetaling.getNetto()).isNull();
//        assertThat(jsonUtbetaling.getUtbetalingsdato()).isNull();
//        assertThat(jsonUtbetaling.getPeriodeFom()).isEqualTo(utbetaling.getPeriodeFom() == null ? null : utbetaling.getPeriodeFom().toString());
//        assertThat(jsonUtbetaling.getPeriodeTom()).isEqualTo(utbetaling.getPeriodeTom() == null ? null : utbetaling.getPeriodeTom().toString());
//        assertThat(jsonUtbetaling.getSkattetrekk()).isEqualTo(utbetaling.getSkattetrekk());
//        assertThat(jsonUtbetaling.getAndreTrekk()).isNull();
//        assertThat(jsonUtbetaling.getOverstyrtAvBruker()).isFalse();
//    }
//}
