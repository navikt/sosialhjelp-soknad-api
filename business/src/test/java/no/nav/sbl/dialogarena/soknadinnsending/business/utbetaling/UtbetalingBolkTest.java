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
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingBolkTest {
    private static final String FNR = "12345678910";
    private static final Long SOKNADID = 123456L;
    private static final String UTBETALINGSID = "Barnetrygd|568269505|2018-02-22";

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
        Map<String, String> utbetalingProperties = faktumListe.get(1).getProperties();
        Map<String, String> utbetalingKomponentProperties = faktumListe.get(2).getProperties();
        Map<String, String> utbetalingKomponent2Properties = faktumListe.get(3).getProperties();

        assertThat(faktumListe.size(), is(4));
        assertThat(faktumListe.get(0).getKey(), is("utbetalinger.ingen"));
        assertThat(faktumListe.get(0).getValue(), is("false"));
        assertThat(faktumListe.get(1).getKey(), is("utbetalinger.utbetaling"));
        assertThat(faktumListe.get(1).getUnikProperty(), is("id"));
        assertThat(utbetalingProperties.get("type"), is("Barnetrygd"));
        assertThat(utbetalingProperties.get("utbetalingsid"), is(UTBETALINGSID));
        assertThat(faktumListe.get(2).getKey(), is("utbetalinger.utbetaling.komponent"));
        assertThat(utbetalingKomponentProperties.get("type"), is("Arbeidstaker"));
        assertThat(utbetalingKomponentProperties.get("utbetalingsid"), is(UTBETALINGSID));
        assertThat(faktumListe.get(3).getKey(), is("utbetalinger.utbetaling.komponent"));
        assertThat(utbetalingKomponent2Properties.get("satstype"), is("UFO"));
        assertThat(utbetalingKomponent2Properties.get("utbetalingsid"), is(UTBETALINGSID));
    }

    private List<Utbetaling> lagUtbetalingsliste() {
        List<Utbetaling> utbetalingsliste = new ArrayList<>();

        Utbetaling utbetaling = new Utbetaling();
        utbetaling.type = "Barnetrygd";
        utbetaling.netto = 3880.0;
        utbetaling.brutto = 3880.0;
        utbetaling.skattetrekk = -0.0;
        utbetaling.andreTrekk = -0.0;
        utbetaling.bilagsnummer = "568269505";
        utbetaling.periodeFom = LocalDate.of(2018, 2, 1);
        utbetaling.periodeFom = LocalDate.of(2018, 2, 28);
        utbetaling.utbetalingsdato = LocalDate.of(2018, 2, 22);

        List<Utbetaling.Komponent> komponentliste = new ArrayList<>();
        Utbetaling.Komponent komponent = new Utbetaling.Komponent();
        komponent.type = "Arbeidstaker";
        komponent.belop = 23620.0;
        komponent.satsType = "Dag";
        komponent.satsBelop = 1181.0;
        komponent.satsAntall = 20;
        komponentliste.add(komponent);

        Utbetaling.Komponent komponent2 = new Utbetaling.Komponent();
        komponent2.type = "Arbeidstaker";
        komponent2.belop = 5067.0;
        komponent2.satsType = "UFO";
        komponent2.satsBelop = 1181.0;
        komponent2.satsAntall = 20;
        komponentliste.add(komponent2);

        utbetaling.komponenter = komponentliste;
        utbetalingsliste.add(utbetaling);
        return utbetalingsliste;
    }

}