package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
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

    private static final String YTELSESTYPE = "Onkel Skrue penger";
    private static final double NETTO = 60000.0;
    private static final double BRUTTO = 3880.0;
    private static final double SKATT = -1337.0;
    private static final double TREKK = -500.0;
    private static final String KOMPONENTTYPE = "Pengesekk";
    private static final double KOMPONENTBELOP = 50000.0;
    private static final String SATSTYPE = "Dag";
    private static final double SATSBELOP = 5000.0;
    private static final double SATSANTALL = 10.0;

    private static final Utbetaling UTBETALING = new Utbetaling();
    private static final Utbetaling.Komponent KOMPONENT = new Utbetaling.Komponent();

    static {
        UTBETALING.type = YTELSESTYPE;
        UTBETALING.netto = NETTO;
        UTBETALING.brutto = BRUTTO;
        UTBETALING.skattetrekk = SKATT;
        UTBETALING.andreTrekk = TREKK;
        UTBETALING.utbetalingsdato = UTBETALINGSDATO;
        UTBETALING.periodeFom = PERIODE_FOM;
        UTBETALING.periodeTom = PERIODE_TOM;

        KOMPONENT.type = KOMPONENTTYPE;
        KOMPONENT.belop = KOMPONENTBELOP;
        KOMPONENT.satsType = SATSTYPE;
        KOMPONENT.satsBelop = SATSBELOP;
        KOMPONENT.satsAntall = SATSANTALL;

        UTBETALING.komponenter = Collections.singletonList(KOMPONENT);
    }

    @Mock
    private UtbetalingService utbetalingService;

    @InjectMocks
    private InntektSystemdata inntektSystemdata;

    @Spy
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
        List<Utbetaling> utbetalinger = Collections.singletonList(UTBETALING);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(utbetalinger);

        inntektSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);

        assertThat(utbetaling.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(UTBETALING, utbetaling);
    }

    @Test
    public void skalOppdatereUtbetalingerUtenAAOverskriveBrukerUtfylteUtbetalinger() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUtbetalinger());
        List<Utbetaling> utbetalinger = Collections.singletonList(UTBETALING);
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(utbetalinger);

        inntektSystemdata.updateSystemdataIn(soknadUnderArbeid);

        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        JsonOkonomiOpplysningUtbetaling utbetaling = jsonUtbetalinger.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling_1 = jsonUtbetalinger.get(1);

        assertThat(utbetaling.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetaling.equals(JSON_OKONOMI_OPPLYSNING_UTBETALING), is(true));
        assertThat(utbetaling_1.getKilde(), is(JsonKilde.SYSTEM));
        assertThatUtbetalingIsCorrectlyConverted(UTBETALING, utbetaling_1);
    }

    private JsonInternalSoknad createJsonInternalSoknadWithUtbetalinger() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        List<JsonOkonomiOpplysningUtbetaling> jsonUtbetalinger = new ArrayList<>();
        jsonUtbetalinger.add(JSON_OKONOMI_OPPLYSNING_UTBETALING);
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().withUtbetaling(jsonUtbetalinger);
        return jsonInternalSoknad;
    }

    private void assertThatUtbetalingIsCorrectlyConverted(Utbetaling utbetaling, JsonOkonomiOpplysningUtbetaling jsonUtbetaling) {
        assertThat("type", jsonUtbetaling.getType(), is("navytelse"));
        assertThat("tittel", jsonUtbetaling.getTittel(), is(utbetaling.type));
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
