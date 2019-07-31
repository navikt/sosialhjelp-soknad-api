package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata.InntektSystemdata.tilIntegerMedAvrunding;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InntektSystemdataTest {

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
    private static final String KOMPONENTTYPE_2 = "Månedslønn";
    private static final double KOMPONENTBELOP_2 = 10000.0;
    private static final String SATSTYPE_2 = "Årslønn";
    private static final double SATSBELOP_2 = 120000.0;
    private static final double SATSANTALL_2 = 12.0;

    private static final Utbetaling NAV_UTBETALING = new Utbetaling();
    private static final Utbetaling.Komponent NAV_KOMPONENT = new Utbetaling.Komponent();
    private static final Utbetaling SKATTBAR_UTBETALING = new Utbetaling();
    private static final Utbetaling.Komponent SKATTBAR_KOMPONENT = new Utbetaling.Komponent();

    static {
        NAV_UTBETALING.tittel = TITTEL;
        NAV_UTBETALING.netto = NETTO;
        NAV_UTBETALING.brutto = BRUTTO;
        NAV_UTBETALING.skattetrekk = SKATT;
        NAV_UTBETALING.andreTrekk = TREKK;
        NAV_UTBETALING.utbetalingsdato = UTBETALINGSDATO;
        NAV_UTBETALING.periodeFom = PERIODE_FOM;
        NAV_UTBETALING.periodeTom = PERIODE_TOM;

        NAV_KOMPONENT.type = KOMPONENTTYPE;
        NAV_KOMPONENT.belop = KOMPONENTBELOP;
        NAV_KOMPONENT.satsType = SATSTYPE;
        NAV_KOMPONENT.satsBelop = SATSBELOP;
        NAV_KOMPONENT.satsAntall = SATSANTALL;

        NAV_UTBETALING.komponenter = Collections.singletonList(NAV_KOMPONENT);

        SKATTBAR_UTBETALING.tittel = TITTEL_2;
        SKATTBAR_UTBETALING.netto = NETTO_2;
        SKATTBAR_UTBETALING.brutto = BRUTTO_2;
        SKATTBAR_UTBETALING.skattetrekk = SKATT_2;
        SKATTBAR_UTBETALING.andreTrekk = TREKK_2;
        SKATTBAR_UTBETALING.utbetalingsdato = UTBETALINGSDATO;
        SKATTBAR_UTBETALING.periodeFom = PERIODE_FOM;
        SKATTBAR_UTBETALING.periodeTom = PERIODE_TOM;

        SKATTBAR_KOMPONENT.type = KOMPONENTTYPE_2;
        SKATTBAR_KOMPONENT.belop = KOMPONENTBELOP_2;
        SKATTBAR_KOMPONENT.satsType = SATSTYPE_2;
        SKATTBAR_KOMPONENT.satsBelop = SATSBELOP_2;
        SKATTBAR_KOMPONENT.satsAntall = SATSANTALL_2;

        SKATTBAR_UTBETALING.komponenter = Collections.singletonList(SKATTBAR_KOMPONENT);
    }

    @Mock
    private UtbetalingService utbetalingService;

    @Mock
    ArbeidsforholdTransformer arbeidsforholdTransformer;

    @InjectMocks
    private InntektSystemdata inntektSystemdata;

    @Mock
    SkattbarInntektService skattbarInntektService;

    @Before
    public void setUp() throws Exception {
        System.setProperty("tillatmock", "true");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("tillatmock", "false");
    }

    @Test
    public void skalOppdatereUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<Utbetaling> nav_utbetalinger = Collections.singletonList(NAV_UTBETALING);
        List<Utbetaling> skattbare_utbetalinger = Collections.singletonList(SKATTBAR_UTBETALING);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(nav_utbetalinger);
        when(skattbarInntektService.hentSkattbarInntekt(anyString())).thenReturn(skattbare_utbetalinger);

        inntektSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING, utbetaling, "navytelse");
        assertThatUtbetalingIsCorrectlyConverted(SKATTBAR_UTBETALING, utbetaling_1, "skatteetaten");
    }

    @Test
    public void skalOppdatereUtbetalingerUtenAAOverskriveBrukerUtfylteUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        List<Utbetaling> utbetalinger = Collections.singletonList(NAV_UTBETALING);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(utbetalinger);

        inntektSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetaling.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(utbetaling_1.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(NAV_UTBETALING, utbetaling_1, "navytelse");
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
