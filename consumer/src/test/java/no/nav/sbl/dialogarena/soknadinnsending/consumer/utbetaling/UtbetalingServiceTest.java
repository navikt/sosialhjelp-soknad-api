package no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock.dato;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingServiceTest {

    private static final String YTELSESTYPE = "Onkel Skrue penger";
    private static final double NETTO = 60000.0;
    private static final double BRUTTO = 3880.0;
    private static final double SKATT = -1337.0;
    private static final double TREKK = -500.0;
    private static final String BILAGSNR = "568269566";
    private static final int YEAR = 2018;
    @Mock
    private UtbetalingV1 utbetalingV1;

    @InjectMocks
    private UtbetalingService utbetalingService;

    private WSUtbetaling komplettUtbetaling;
    private WSYtelse komplettYtelse;
    private WSPerson wsPerson;

    @Before
    public void setUp() {
        wsPerson = new WSPerson()
                .withAktoerId("12345678910")
                .withNavn("Dummy");

        komplettUtbetaling = lagKomplettWSUtbetaling(wsPerson);
        komplettYtelse = lagKomplettWsYtelse(wsPerson);
    }

    @Test
    public void ytelseTilUtbetalingMapperMaksimalWSUtbetalingerRiktig() {
        Utbetaling utbetaling = utbetalingService.ytelseTilUtbetaling(komplettUtbetaling, komplettYtelse);

        assertThat(utbetaling.type, is(YTELSESTYPE));
        assertThat(utbetaling.netto, is(NETTO));
        assertThat(utbetaling.brutto, is(BRUTTO));
        assertThat(utbetaling.skatteTrekk, is(SKATT));
        assertThat(utbetaling.andreTrekk, is(TREKK));
        assertThat(utbetaling.bilagsNummer, is(BILAGSNR));
        assertThat(utbetaling.periodeFom.getYear(), is(YEAR));
    }

    private WSUtbetaling lagKomplettWSUtbetaling(WSPerson wsPerson) {
        WSBankkonto bankkonto = new WSBankkonto()
                .withKontotype("Norsk bankkonto")
                .withKontonummer("32902095534");
        return new WSUtbetaling()
                .withPosteringsdato(dato(YEAR, 2, 21))
                .withUtbetaltTil(wsPerson)
                .withUtbetalingNettobeloep(3880.0)
                .withYtelseListe(lagKomplettWsYtelse(wsPerson))
                .withUtbetalingsdato(dato(YEAR, 2, 27))
                .withForfallsdato(dato(YEAR, 2, 28))
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
                        .withFom(dato(YEAR, 2, 1))
                        .withTom(dato(YEAR, 2, 28)))
                .withYtelseskomponentListe(new WSYtelseskomponent()
                                .withYtelseskomponenttype("Sjekk")
                                .withSatsbeloep(0.0)
                                .withYtelseskomponentbeloep(10000.37),
                        new WSYtelseskomponent()
                                .withYtelseskomponenttype("Pengesekk")
                                .withSatstype("Dag")
                                .withSatsbeloep(5000.0)
                                .withSatsantall(10.0)
                                .withYtelseskomponentbeloep(50000.0))
                .withYtelseskomponentersum(BRUTTO)
                .withTrekksum(TREKK)
                .withSkattsum(SKATT)
                .withYtelseNettobeloep(NETTO)
                .withBilagsnummer(BILAGSNR)
                .withRettighetshaver(person)
                .withRefundertForOrg(dummyOrg);
    }

}