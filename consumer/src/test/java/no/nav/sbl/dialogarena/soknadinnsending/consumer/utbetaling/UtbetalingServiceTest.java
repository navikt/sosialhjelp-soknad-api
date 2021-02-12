package no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling;

import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.utbetaling.v1.meldinger.WSHentUtbetalingsinformasjonResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock.dato;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingServiceTest {
    private static final String YTELSESTYPE = "navytelse";
    private static final String TITTEL = "navytelse";
    private static final double NETTO = 60000.0;
    private static final double BRUTTO = 3880.0;
    private static final double SKATT = -1337.0;
    private static final double TREKK = -500.0;
    private static final String BILAGSNR = "568269566";
    private static final int YEAR = 2018;
    private static final int MONTH = 2;
    private static final int FOM_DATE = 1;
    private static final int TOM_DATE = 23;
    private static final int UTBETALT_DAG = 27;
    private static final String KOMPONENTTYPE = "Pengesekk";
    private static final double KOMPONENTBELOP = 50000.0;
    private static final String SATSTYPE = "Dag";
    private static final double SATSBELOP = 5000.0;
    private static final double SATSANTALL = 10.0;
    private static final int POSTERINGSDAG = 21;
    private static final int FORFALLSDAG = 28;

    @Mock
    private UtbetalingV1 utbetalingV1;

    @InjectMocks
    private UtbetalingService utbetalingService;

    private WSPerson wsPerson;

    @Before
    public void setUp() {
        wsPerson = new WSPerson()
                .withAktoerId("12345678910")
                .withNavn("Dummy");
    }

    @Test
    public void mapTilUtbetalingerGodtarWSUtbetalingUtenYtelseliste() {
        List<Utbetaling> utbetalinger = utbetalingService.mapTilUtbetalinger(lagWSHentUtbetalingsinformasjonResponseUtenYtelseliste(wsPerson));

        assertThat(utbetalinger.size(), is(0));
    }

    @Test
    public void mapTilUtbetalingerIgnorererUtbetalingerSomIkkeErUtbetalt() {
        List<Utbetaling> utbetalinger = utbetalingService.mapTilUtbetalinger(lagWSHentUtbetalingsinformasjonResponseUtenUtbetalingsdato(wsPerson));

        assertThat(utbetalinger.size(), is(0));
    }

    @Test
    public void ytelseTilUtbetalingMapperMaksimalWSUtbetalingerRiktig() {
        Utbetaling utbetaling = utbetalingService.ytelseTilUtbetaling(lagKomplettWSUtbetaling(wsPerson), lagKomplettWsYtelse(wsPerson));
        Utbetaling.Komponent komponent = utbetaling.komponenter.get(1);

        assertThat(utbetaling.type, is(YTELSESTYPE));
        assertThat(utbetaling.tittel, is(TITTEL));
        assertThat(utbetaling.netto, is(NETTO));
        assertThat(utbetaling.brutto, is(BRUTTO));
        assertThat(utbetaling.skattetrekk, is(SKATT));
        assertThat(utbetaling.andreTrekk, is(TREKK));
        assertThat(utbetaling.bilagsnummer, is(BILAGSNR));
        assertThat(utbetaling.periodeFom.getYear(), is(YEAR));
        assertThat(utbetaling.periodeFom.getMonthValue(), is(MONTH));
        assertThat(utbetaling.periodeFom.getDayOfMonth(), is(FOM_DATE));
        assertThat(utbetaling.periodeTom.getDayOfMonth(), is(TOM_DATE));
        assertThat(utbetaling.utbetalingsdato.getDayOfMonth(), is(UTBETALT_DAG));

        assertThat(komponent.type, is(KOMPONENTTYPE));
        assertThat(komponent.belop, is(KOMPONENTBELOP));
        assertThat(komponent.satsType, is(SATSTYPE));
        assertThat(komponent.satsBelop, is(SATSBELOP));
        assertThat(komponent.satsAntall, is(SATSANTALL));
    }

    @Test
    public void ytelseTilUtbetalingMapperMinimalWSUtbetalingRiktig() {
        Utbetaling utbetaling = utbetalingService.ytelseTilUtbetaling(lagMinimalWSUtbetaling(wsPerson), lagMinimalWsYtelseMedYtelseskomponentliste(wsPerson));
        Utbetaling.Komponent komponent = utbetaling.komponenter.get(0);

        assertThat(utbetaling.type, is(YTELSESTYPE));
        assertThat(utbetaling.netto, is(0.0));
        assertThat(utbetaling.brutto, is(0.0));
        assertThat(utbetaling.skattetrekk, is(0.0));
        assertThat(utbetaling.andreTrekk, is(0.0));
        assertThat(utbetaling.bilagsnummer, is(nullValue()));
        assertThat(utbetaling.periodeFom.getYear(), is(YEAR));
        assertThat(utbetaling.periodeFom.getMonthValue(), is(MONTH));
        assertThat(utbetaling.periodeFom.getDayOfMonth(), is(FOM_DATE));
        assertThat(utbetaling.periodeTom, is(nullValue()));
        assertThat(utbetaling.utbetalingsdato.getDayOfMonth(), is(UTBETALT_DAG));

        assertThat(komponent.type, is(KOMPONENTTYPE));
        assertThat(komponent.belop, is(0.0));
        assertThat(komponent.satsType, nullValue());
        assertThat(komponent.satsBelop, is(0.0));
        assertThat(komponent.satsAntall, is(0.0));
    }

    private WSHentUtbetalingsinformasjonResponse lagWSHentUtbetalingsinformasjonResponseUtenYtelseliste(WSPerson wsPerson) {
        List<WSUtbetaling> wsUtbetalinger = new ArrayList<>();
        wsUtbetalinger.add(lagWSUtbetalingUtenYtelsesliste(wsPerson));
        return new WSHentUtbetalingsinformasjonResponse().withUtbetalingListe(wsUtbetalinger);
    }

    private WSHentUtbetalingsinformasjonResponse lagWSHentUtbetalingsinformasjonResponseUtenUtbetalingsdato(WSPerson wsPerson) {
        List<WSUtbetaling> wsUtbetalinger = new ArrayList<>();
        wsUtbetalinger.add(lagWSUtbetalingSomIkkeErUtbetalt(wsPerson));
        return new WSHentUtbetalingsinformasjonResponse().withUtbetalingListe(wsUtbetalinger);
    }

    private WSHentUtbetalingsinformasjonResponse lagWSHentUtbetalingsinformasjonResponseMedEnUtbetalingUtenforPeriode(WSPerson wsPerson) {
        List<WSUtbetaling> wsUtbetalinger = new ArrayList<>();
        final LocalDateTime posteringsdato = LocalDateTime.now().minusDays(40);
        final LocalDateTime utbetalingsDatoUtenforPeriode = LocalDateTime.now().minusDays(35);
        final LocalDateTime utbetalingsDatoInnenforPeriode = LocalDateTime.now().minusDays(30);
        wsUtbetalinger.add(lagWSUtbetaling(wsPerson, posteringsdato, utbetalingsDatoUtenforPeriode));
        wsUtbetalinger.add(lagWSUtbetaling(wsPerson, posteringsdato, utbetalingsDatoInnenforPeriode));
        return new WSHentUtbetalingsinformasjonResponse().withUtbetalingListe(wsUtbetalinger);
    }

    private WSUtbetaling lagWSUtbetalingSomIkkeErUtbetalt(WSPerson wsPerson) {
        WSBankkonto bankkonto = new WSBankkonto()
                .withKontotype("Norsk bankkonto")
                .withKontonummer("123456789123");
        return new WSUtbetaling()
                .withPosteringsdato(dato(YEAR, MONTH, POSTERINGSDAG))
                .withUtbetaltTil(wsPerson)
                .withUtbetalingNettobeloep(3880.0)
                .withYtelseListe(lagKomplettWsYtelse(wsPerson))
                .withForfallsdato(dato(YEAR, MONTH, FORFALLSDAG))
                .withUtbetaltTilKonto(bankkonto)
                .withUtbetalingsmelding(null)
                .withUtbetalingsmetode("Norsk bankkonto")
                .withUtbetalingsstatus("Ikke utbetalt");
    }

    private WSUtbetaling lagWSUtbetaling(WSPerson wsPerson, LocalDateTime posteringsdato, LocalDateTime utbetalingsdato) {
        WSBankkonto bankkonto = new WSBankkonto()
                .withKontotype("Norsk bankkonto")
                .withKontonummer("123456789123");
        return new WSUtbetaling()
                .withPosteringsdato(dato(posteringsdato))
                .withUtbetaltTil(wsPerson)
                .withUtbetalingNettobeloep(3880.0)
                .withYtelseListe(lagKomplettWsYtelse(wsPerson))
                .withUtbetalingsdato(dato(utbetalingsdato))
                .withUtbetaltTilKonto(bankkonto)
                .withUtbetalingsmelding(null)
                .withUtbetalingsmetode("Norsk bankkonto")
                .withUtbetalingsstatus("Ikke utbetalt");
    }

    private WSUtbetaling lagWSUtbetalingUtenYtelsesliste(WSPerson wsPerson) {
        return new WSUtbetaling()
                .withPosteringsdato(dato(YEAR, MONTH, POSTERINGSDAG))
                .withUtbetaltTil(wsPerson)
                .withUtbetalingsdato(dato(YEAR, MONTH, UTBETALT_DAG))
                .withUtbetalingsmetode("Norsk bankkonto")
                .withUtbetalingsstatus("Utbetalt");
    }

    private WSUtbetaling lagKomplettWSUtbetaling(WSPerson wsPerson) {
        WSBankkonto bankkonto = new WSBankkonto()
                .withKontotype("Norsk bankkonto")
                .withKontonummer("123456789123");
        return new WSUtbetaling()
                .withPosteringsdato(dato(YEAR, MONTH, POSTERINGSDAG))
                .withUtbetaltTil(wsPerson)
                .withUtbetalingNettobeloep(3880.0)
                .withYtelseListe(lagKomplettWsYtelse(wsPerson))
                .withUtbetalingsdato(dato(YEAR, MONTH, UTBETALT_DAG))
                .withForfallsdato(dato(YEAR, MONTH, FORFALLSDAG))
                .withUtbetaltTilKonto(bankkonto)
                .withUtbetalingsmelding(null)
                .withUtbetalingsmetode("Norsk bankkonto")
                .withUtbetalingsstatus("Utbetalt");
    }

    private WSYtelse lagKomplettWsYtelse(WSPerson person) {
        WSOrganisasjon dummyOrg = new WSOrganisasjon()
                .withAktoerId("000000000");
        return new WSYtelse()
                .withYtelsestype(new WSYtelsestyper().withValue(YTELSESTYPE))
                .withYtelsesperiode(new WSPeriode()
                        .withFom(dato(YEAR, MONTH, FOM_DATE))
                        .withTom(dato(YEAR, MONTH, TOM_DATE)))
                .withYtelseskomponentListe(
                        new WSYtelseskomponent()
                                .withYtelseskomponenttype("Sjekk")
                                .withSatsbeloep(0.0)
                                .withYtelseskomponentbeloep(10000.37),
                        new WSYtelseskomponent()
                                .withYtelseskomponenttype(KOMPONENTTYPE)
                                .withSatstype(SATSTYPE)
                                .withSatsbeloep(SATSBELOP)
                                .withSatsantall(SATSANTALL)
                                .withYtelseskomponentbeloep(KOMPONENTBELOP))
                .withYtelseskomponentersum(BRUTTO)
                .withTrekksum(TREKK)
                .withSkattsum(SKATT)
                .withYtelseNettobeloep(NETTO)
                .withBilagsnummer(BILAGSNR)
                .withRettighetshaver(person)
                .withRefundertForOrg(dummyOrg);
    }

    private WSUtbetaling lagMinimalWSUtbetaling(WSPerson wsPerson) {
        return new WSUtbetaling()
                .withPosteringsdato(dato(YEAR, MONTH, POSTERINGSDAG))
                .withUtbetaltTil(wsPerson)
                .withUtbetalingsdato(dato(YEAR, MONTH, UTBETALT_DAG))
                .withYtelseListe(lagMinimalWsYtelseMedYtelseskomponentliste(wsPerson))
                .withUtbetalingsmetode("Norsk bankkonto")
                .withUtbetalingsstatus("Utbetalt");
    }

    private WSYtelse lagMinimalWsYtelseMedYtelseskomponentliste(WSPerson person) {
        return new WSYtelse()
                .withYtelsestype(new WSYtelsestyper().withValue(YTELSESTYPE))
                .withYtelsesperiode(new WSPeriode()
                        .withFom(dato(YEAR, MONTH, FOM_DATE)))
                .withYtelseskomponentListe(
                        new WSYtelseskomponent()
                                .withYtelseskomponenttype(KOMPONENTTYPE))
                .withRettighetshaver(person);
    }

}