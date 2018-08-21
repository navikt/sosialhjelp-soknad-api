package no.nav.sbl.dialogarena.soknadinnsending.business.utbetaling;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingBolkTest {
    private static final String FNR = "12345678910";
    private static final Long SOKNADID = 123456L;

    @Mock
    private UtbetalingService utbetalingService;

    @InjectMocks
    private UtbetalingBolk utbetalingBolk;

    @Test
    public void genererSystemFaktaReturnererUtbetalingFeiletFaktumHvisTjenestekallFeiler() {
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(), any())).thenReturn(null);

        List<Faktum> faktumListe = utbetalingBolk.genererSystemFakta(FNR, SOKNADID);

        assertThat(faktumListe.size(), is(1));
        assertThat(faktumListe.get(0).getKey(), is("utbetalinger.feilet"));
        assertThat(faktumListe.get(0).getValue(), is("true"));
    }

    @Test
    public void genererSystemFaktaReturnererIngenUtbetalingerFaktumForBrukerUtenUtbetalinger() {
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(), any())).thenReturn(new ArrayList<>());

        List<Faktum> faktumListe = utbetalingBolk.genererSystemFakta(FNR, SOKNADID);

        assertThat(faktumListe.size(), is(1));
        assertThat(faktumListe.get(0).getKey(), is("utbetalinger.ingen"));
        assertThat(faktumListe.get(0).getValue(), is("true"));
    }

    @Test
    public void genererSystemFaktaReturnererFaktumlisteMedUtbetalingerFaktumForBrukerMedUtbetalinger() {
        when(utbetalingService.hentUtbetalingerForBrukerIPeriode(anyString(), any(), any())).thenReturn(lagUtbetalingsliste());

        List<Faktum> faktumListe = utbetalingBolk.genererSystemFakta(FNR, SOKNADID);

        assertThat(faktumListe.size(), is(2));
        assertThat(faktumListe.get(0).getKey(), is("utbetalinger.ingen"));
        assertThat(faktumListe.get(0).getValue(), is("false"));
        assertThat(faktumListe.get(1).getKey(), is("utbetalinger.utbetaling"));
        assertThat(faktumListe.get(1).getUnikProperty(), is("id"));
        assertThat(faktumListe.get(1).getProperties().get("type"), is("Barnetrygd"));
    }

    private List<Utbetaling> lagUtbetalingsliste() {
        List<Utbetaling> utbetalingsliste = new ArrayList<>();

        Utbetaling utbetaling = new Utbetaling();
        utbetaling.type = "Barnetrygd";
        utbetaling.netto = 3880.0;
        utbetaling.brutto = 3880.0;
        utbetaling.skatteTrekk = -0.0;
        utbetaling.andreTrekk = -0.0;
        utbetaling.bilagsNummer = "568269505";
        utbetaling.periodeFom = LocalDate.of(2018, 2, 1);
        utbetaling.periodeFom = LocalDate.of(2018, 2, 28);
        utbetaling.utbetalingsDato = LocalDate.of(2018, 2, 22);

        List<Utbetaling.Komponent> komponentliste = new ArrayList<>();
        Utbetaling.Komponent komponent = new Utbetaling.Komponent();
        komponent.type = "Arbeidstaker";
        komponent.belop = 23620.0;
        komponent.satsType = "Dag";
        komponent.satsBelop = 1181.0;
        komponent.satsAntall = 20;
        komponentliste.add(komponent);

        utbetaling.komponenter = komponentliste;
        utbetalingsliste.add(utbetaling);
        return utbetalingsliste;
    }

}