package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent;
import org.junit.Test;

import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonOkonomiOpplysningerConverter.opplysningUtbetalingFraNav;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonOkonomiOpplysningerConverter.tilUtbetalingskomponentListe;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.*;

public class JsonOkonomiOpplysningerConverterTest {
    private static final Long SOKNADID = 1L;
    private static final String UTBETALINGSID = "Barnetrygd|568269505|2018-02-22";
    private static final String UTBETALINGSID2 = "Alderspensjon|874252598|2018-02-12";
    private static final String TYPE = "Arbeidsavklaringspenger";
    private static final String TYPE2 = "Dagpenger";
    private static final String UTBETALINGSDATO = "2018-02-22";
    private static final String PERIODE_FOM = "2018-02-28";
    private static final String KOMPONENTTYPE = "Arbeidstaker";
    private static final String SATSTYPE = "Dag";
    private static final String SATSTYPE2 = "Ufo";

    @Test
    public void opplysningUtbetalingFraNavReturnererTomListeForSoknadUtenUtbetalinger() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("utbetalinger.ingen").medValue("true"));

        List<JsonOkonomiOpplysningUtbetaling> utbetalingerFraNav = opplysningUtbetalingFraNav(webSoknad);

        assertThat(utbetalingerFraNav, empty());
    }

    @Test
    public void opplysningUtbetalingFraNavLagerRiktigJsonObjektForUtbetalingUtenKomponentliste() {
        final WebSoknad webSoknad = new WebSoknad().medFaktum(lagFaktumForUtbetaling(UTBETALINGSID, TYPE));

        List<JsonOkonomiOpplysningUtbetaling> utbetalingerFraNav = opplysningUtbetalingFraNav(webSoknad);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalingerFraNav.get(0);

        assertThat(utbetalingerFraNav.size(), is(1));
        assertThat(utbetaling.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(utbetaling.getType(), is("navytelse"));
        assertThat(utbetaling.getTittel(), is(TYPE));
        assertThat(utbetaling.getBelop(), is(3880));
        assertThat(utbetaling.getNetto(), is(3880.4));
        assertThat(utbetaling.getBrutto(), is(3892.0));
        assertThat(utbetaling.getSkattetrekk(), is(-0.0));
        assertThat(utbetaling.getAndreTrekk(), is(-250.0));
        assertThat(utbetaling.getUtbetalingsdato(), is(UTBETALINGSDATO));
        assertThat(utbetaling.getPeriodeFom(), is(PERIODE_FOM));
        assertThat(utbetaling.getPeriodeTom(), nullValue());
        assertThat(utbetaling.getKomponenter(), empty());
        assertThat(utbetaling.getOverstyrtAvBruker(), is(false));
    }

    @Test
    public void opplysningUtbetalingFraNavLagerRiktigJsonObjektForToUtbetalingerMedKomponentliste() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(lagFaktumForUtbetaling(UTBETALINGSID, TYPE))
                .medFaktum(lagFaktumForUtbetaling(UTBETALINGSID2, TYPE2))
                .medFaktum(lagFaktumForUtbetalingskomponent("0", UTBETALINGSID, SATSTYPE, "23 620,00"));

        List<JsonOkonomiOpplysningUtbetaling> utbetalingerFraNav = opplysningUtbetalingFraNav(webSoknad);
        JsonOkonomiOpplysningUtbetaling utbetaling = utbetalingerFraNav.get(0);
        JsonOkonomiOpplysningUtbetaling utbetaling2 = utbetalingerFraNav.get(1);

        assertThat(utbetalingerFraNav.size(), is(2));
        assertThat(utbetaling.getTittel(), is(TYPE));
        assertThat(utbetaling.getKomponenter().size(), is(1));
        assertThat(utbetaling.getKomponenter().get(0).getSatsType(), is(SATSTYPE));
        assertThat(utbetaling2.getTittel(), is(TYPE2));
    }

    @Test
    public void tilUtbetalingskomponentListeLagerRiktigJsonObjektForKomponentlisteMedToKomponenter() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(lagFaktumForUtbetalingskomponent("0", UTBETALINGSID, SATSTYPE, "23 620,00"))
                .medFaktum(lagFaktumForUtbetalingskomponent("1", UTBETALINGSID, SATSTYPE2, "10 652,50"));

        List<JsonOkonomiOpplysningUtbetalingKomponent> utbetalingskomponenter = tilUtbetalingskomponentListe(webSoknad, UTBETALINGSID);
        JsonOkonomiOpplysningUtbetalingKomponent komponent = utbetalingskomponenter.get(0);
        JsonOkonomiOpplysningUtbetalingKomponent komponent2 = utbetalingskomponenter.get(1);

        assertThat(utbetalingskomponenter.size(), is(2));
        assertThat(komponent.getBelop(), is(23620.0));
        assertThat(komponent.getType(), is(KOMPONENTTYPE));
        assertThat(komponent.getSatsBelop(), is(1181.0));
        assertThat(komponent.getSatsType(), is(SATSTYPE));
        assertThat(komponent.getSatsAntall(), is(20.0));
        assertThat(komponent2.getBelop(), is(10652.5));
        assertThat(komponent2.getType(), is(KOMPONENTTYPE));
        assertThat(komponent2.getSatsBelop(), is(1181.0));
        assertThat(komponent2.getSatsType(), is(SATSTYPE2));
        assertThat(komponent2.getSatsAntall(), is(20.0));
    }

    @Test
    public void tilUtbetalingskomponentListeSerBortFraKomponenterMedAnnenUtbetalingsid() {
        final WebSoknad webSoknad = new WebSoknad()
                .medFaktum(lagFaktumForUtbetalingskomponent("0", UTBETALINGSID, SATSTYPE, "23 620,00"))
                .medFaktum(lagFaktumForUtbetalingskomponent("1", UTBETALINGSID2, SATSTYPE2, "10 652,50"));

        List<JsonOkonomiOpplysningUtbetalingKomponent> utbetalingskomponenter = tilUtbetalingskomponentListe(webSoknad, UTBETALINGSID);

        assertThat(utbetalingskomponenter.size(), is(1));
    }

    private Faktum lagFaktumForUtbetaling(String utbetalingsid, String type) {
        return new Faktum()
                .medSoknadId(SOKNADID)
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.utbetaling")
                .medUnikProperty("id")
                .medSystemProperty("id", utbetalingsid)
                .medSystemProperty("utbetalingsid", utbetalingsid)
                .medSystemProperty("type", type)
                .medSystemProperty("netto", "3 880,40")
                .medSystemProperty("brutto", "3 892,00")
                .medSystemProperty("skatteTrekk", "-0,00")
                .medSystemProperty("andreTrekk", "-250,00")
                .medSystemProperty("periodeFom", PERIODE_FOM)
                .medSystemProperty("periodeTom", null)
                .medSystemProperty("utbetalingsDato", UTBETALINGSDATO);
    }

    private Faktum lagFaktumForUtbetalingskomponent(String id, String utbetalingsid, String satstype, String belop) {
        return new Faktum()
                .medSoknadId(SOKNADID)
                .medType(SYSTEMREGISTRERT)
                .medKey("utbetalinger.utbetaling.komponent")
                .medUnikProperty("id")
                .medSystemProperty("id", id)
                .medSystemProperty("utbetalingsid", utbetalingsid)
                .medSystemProperty("type", KOMPONENTTYPE)
                .medSystemProperty("belop", belop)
                .medSystemProperty("satstype", satstype)
                .medSystemProperty("satsbelop", "1 181,00")
                .medSystemProperty("satsantall", "20,00");
    }
}