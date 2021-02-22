package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sosialhjelp.soknad.consumer.organisasjon.OrganisasjonService;
import no.nav.sosialhjelp.soknad.consumer.utbetaling.UtbetalingService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
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

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_NAVYTELSE;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sosialhjelp.soknad.business.service.systemdata.UtbetalingerFraNavSystemdata.tilIntegerMedAvrunding;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingerFraNavSystemdataTest {

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

    private static final Utbetaling.Komponent NAV_KOMPONENT = new Utbetaling.Komponent();
    private static final Utbetaling NAV_UTBETALING_1 = new Utbetaling();
    private static final Utbetaling NAV_UTBETALING_2 = new Utbetaling();
    private static final Utbetaling NAV_UTBETALING_3 = new Utbetaling();

    static {
        NAV_UTBETALING_1.tittel = TITTEL;
        NAV_UTBETALING_1.netto = NETTO;
        NAV_UTBETALING_1.brutto = BRUTTO;
        NAV_UTBETALING_1.skattetrekk = SKATT;
        NAV_UTBETALING_1.andreTrekk = TREKK;
        NAV_UTBETALING_1.utbetalingsdato = UTBETALINGSDATO;
        NAV_UTBETALING_1.periodeFom = PERIODE_FOM;
        NAV_UTBETALING_1.periodeTom = PERIODE_TOM;

        NAV_KOMPONENT.type = KOMPONENTTYPE;
        NAV_KOMPONENT.belop = KOMPONENTBELOP;
        NAV_KOMPONENT.satsType = SATSTYPE;
        NAV_KOMPONENT.satsBelop = SATSBELOP;
        NAV_KOMPONENT.satsAntall = SATSANTALL;

        NAV_UTBETALING_1.komponenter = Collections.singletonList(NAV_KOMPONENT);

        NAV_UTBETALING_2.tittel = TITTEL_2;
        NAV_UTBETALING_2.netto = NETTO_2;
        NAV_UTBETALING_2.brutto = BRUTTO_2;
        NAV_UTBETALING_2.skattetrekk = SKATT_2;
        NAV_UTBETALING_2.andreTrekk = TREKK_2;
        NAV_UTBETALING_2.utbetalingsdato = UTBETALINGSDATO;
        NAV_UTBETALING_2.periodeFom = PERIODE_FOM;
        NAV_UTBETALING_2.periodeTom = PERIODE_TOM;
        NAV_UTBETALING_2.orgnummer = ORGANISASJONSNR;

        NAV_UTBETALING_3.tittel = TITTEL_2;
        NAV_UTBETALING_3.netto = NETTO_2;
        NAV_UTBETALING_3.brutto = BRUTTO_2;
        NAV_UTBETALING_3.skattetrekk = SKATT_2;
        NAV_UTBETALING_3.andreTrekk = TREKK_2;
        NAV_UTBETALING_3.utbetalingsdato = UTBETALINGSDATO;
        NAV_UTBETALING_3.periodeFom = PERIODE_FOM;
        NAV_UTBETALING_3.periodeTom = PERIODE_TOM;
        NAV_UTBETALING_3.orgnummer = PERSONNR;

        NAV_UTBETALING_2.komponenter = Collections.singletonList(NAV_KOMPONENT);
    }

    @Mock
    private UtbetalingService utbetalingService;

    @Mock
    OrganisasjonService organisasjonService;

    @InjectMocks
    private UtbetalingerFraNavSystemdata utbetalingerFraNavSystemdata;

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
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<Utbetaling> nav_utbetalinger = Arrays.asList(NAV_UTBETALING_1, NAV_UTBETALING_2);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(nav_utbetalinger);

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_1, utbetaling, UTBETALING_NAVYTELSE);
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_2, utbetaling_1, UTBETALING_NAVYTELSE);
    }

    @Test
    public void skalKunInkludereGyldigeOrganisasjonsnummer() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<Utbetaling> nav_utbetalinger = Arrays.asList(NAV_UTBETALING_1, NAV_UTBETALING_2, NAV_UTBETALING_3);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(nav_utbetalinger);

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

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
    public void skalIkksLasteNedUtbetalingerUtenSamtykke() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        List<Utbetaling> utbetalinger = Collections.singletonList(NAV_UTBETALING_1);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(utbetalinger);

        utbetalingerFraNavSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetaling.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(utbetaling_1.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING_1, utbetaling_1, UTBETALING_NAVYTELSE);
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUtbetalinger() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = new ArrayList<>();
        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().withUtbetaling(jsonUtbetalinger);
        return jsonInternalSoknad;
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
