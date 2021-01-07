package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt.SkattbarInntektService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.UtbetalingerFraNavSystemdata.tilIntegerMedAvrunding;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SkattetatenSystemdataTest {
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
    OrganisasjonService organisasjonService;

    @InjectMocks
    private SkattetatenSystemdata skattetatenSystemdata;

    @Mock
    SkattbarInntektService skattbarInntektService;

    @Before
    public void setUp() {
        System.setProperty("tillatmock", "true");
    }

    @After
    public void tearDown() {
        System.setProperty("tillatmock", "false");
    }

    @Test
    public void skalOppdatereUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> skattbare_utbetalinger = Arrays.asList(SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_ANNEN);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(skattbare_utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING, utbetaling, UTBETALING_SKATTEETATEN);
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling_1, UTBETALING_SKATTEETATEN);
    }

    @Test
    public void skalKunInkludereGyldigeOrganisasjonsnummer() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> skattbare_utbetalinger = Arrays.asList(SKATTBAR_UTBETALING_ANNEN, SKATTBAR_UTBETALING, SKATTBAR_UTBETALING_FRA_PRIVATPERSON);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(skattbare_utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);
        JsonOkonomiOpplysningUtbetaling utbetaling_2 = jsonUtbetalinger.get(2);

        assertEquals(3, jsonUtbetalinger.size());
        assertNull(utbetaling.getOrganisasjon());
        assertEquals(ORGANISASJONSNR, utbetaling_1.getOrganisasjon().getOrganisasjonsnummer());
        assertNull(utbetaling_2.getOrganisasjon());
    }

    @Test
    public void skalOppdatereUtbetalingerUtenAAOverskriveBrukerUtfylteUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING_ANNEN);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetaling.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(utbetaling_1.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetaling_1, UTBETALING_SKATTEETATEN);
    }

    @Test
    public void skalIkkeHenteUtbetalingerUtenSamtykke() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), false);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);

        assertThat(utbetaling.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetaling.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(jsonUtbetalinger.size(), is(1));
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet(), is(false));
    }

    @Test
    public void skalFjerneUtbetalingerNarViIkkeHarSamtykke() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        List<Utbetaling> utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING_ANNEN);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(utbetalinger);

        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalingerA = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetalingA = jsonUtbetalingerA.get(0);
        JsonOkonomiOpplysningUtbetaling utbetalingA_1 = jsonUtbetalingerA.get(1);

        //SJEKK STATE FOR TEST:
        assertThat(utbetalingA.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetalingA.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(utbetalingA_1.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING_ANNEN, utbetalingA_1, UTBETALING_SKATTEETATEN);

        //TEST:
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), false);
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalingerB = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetalingB = jsonUtbetalingerB.get(0);

        assertThat(utbetalingB.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetalingB.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(jsonUtbetalingerB.size(), is(1));
    }

    @Test
    public void updateSystemdata_soknadBlirOppdatertRiktigVedKommunikasjonsfeil_ogBeholderGamleData() {
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
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        assertThat(utbetalinger.size(), is(1));
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalinger.get(0);
        assertThat(utbetaling.getKilde(), is(JSON_OKONOMI_OPPLYSNING_UTBETALING.getKilde()));
        assertThat(utbetaling.getType(), is(JSON_OKONOMI_OPPLYSNING_UTBETALING.getType()));
        assertThat(utbetaling.getBelop(), is(JSON_OKONOMI_OPPLYSNING_UTBETALING.getBelop()));
        assertThat(soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().getInntektFraSkatteetatenFeilet(), is(true));
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
        assertThat("type", jsonUtbetaling.getType(), is(type));
        assertThat("tittel", jsonUtbetaling.getTittel(), is(utbetaling.tittel));
        assertThat("belop", jsonUtbetaling.getBelop(), is(tilIntegerMedAvrunding(String.valueOf(utbetaling.netto))));
        assertThat("brutto", jsonUtbetaling.getBrutto(), is(utbetaling.brutto));
        assertThat("netto", jsonUtbetaling.getNetto(), is(utbetaling.netto));
        assertThat("utbetalingsdato", jsonUtbetaling.getUtbetalingsdato(), is(utbetaling.utbetalingsdato.toString()));
        assertThat("fom", jsonUtbetaling.getPeriodeFom(), is(utbetaling.periodeFom.toString()));
        assertThat("tom", jsonUtbetaling.getPeriodeTom(), is(utbetaling.periodeTom.toString()));
        assertThat("skattetrekk", jsonUtbetaling.getSkattetrekk(), is(utbetaling.skattetrekk));
        assertThat("andreTrekk", jsonUtbetaling.getAndreTrekk(), is(utbetaling.andreTrekk));
        assertThat("overstyrtAvBruker", jsonUtbetaling.getOverstyrtAvBruker(), is(false));
        if (!utbetaling.komponenter.isEmpty()) {
            for (int i = 0; i < utbetaling.komponenter.size(); i++) {
                Utbetaling.Komponent komponent = utbetaling.komponenter.get(i);
                JsonOkonomiOpplysningUtbetalingKomponent jsonKomponent = jsonUtbetaling.getKomponenter().get(i);
                assertThat("komponentType", jsonKomponent.getType(), is(komponent.type));
                assertThat("komponentBelop", jsonKomponent.getBelop(), is(komponent.belop));
                assertThat("komponentSatsType", jsonKomponent.getSatsType(), is(komponent.satsType));
                assertThat("komponentSatsAntall", jsonKomponent.getSatsAntall(), is(komponent.satsAntall));
                assertThat("komponentSatsBelop", jsonKomponent.getSatsBelop(), is(komponent.satsBelop));
            }
        }
    }
}
