package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseObligatoriskSamling;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Test;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.InnsendingsintervallerKodeverk.uke;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.assertj.core.api.Assertions.assertThat;

public class SamlingReiseTilXmlTest {

    @Test
    public void skalKonvertereFaktumStrukturMedRiktigPeriodeForToPerioder() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger").medValue("flere"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-02-01").medProperty("tom", "2015-03-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-01-01").medProperty("tom", "2015-01-02"));

        ReiseObligatoriskSamling samling = new SamlingReiseTilXml().transform(soknad);

        assertThat(samling.getPeriode().getFom()).is(periodeMatcher(2015, 1, 1));
        assertThat(samling.getPeriode().getTom()).is(periodeMatcher(2015, 3, 2));
    }

    @Test
    public void skalKonvertereFaktumStrukturMedRiktigPeriodeHvorEnePeriodenHarBadeTidligstFomOgSenestTom() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger").medValue("flere"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-02-01").medProperty("tom", "2015-03-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-01-01").medProperty("tom", "2015-03-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-01-02").medProperty("tom", "2015-03-01"));

        ReiseObligatoriskSamling samling = new SamlingReiseTilXml().transform(soknad);

        assertThat(samling.getPeriode().getFom()).is(periodeMatcher(2015, 1, 1));
        assertThat(samling.getPeriode().getTom()).is(periodeMatcher(2015, 3, 2));
    }

    @Test
    public void skalKonvertereFaktumStrukturMedRiktigPeriodeForEnSamling() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger").medValue("en"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-02-01").medProperty("tom", "2015-03-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling").medProperty("fom", "2015-01-01").medProperty("tom", "2015-01-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.aktivitetsperiode").medProperty("fom", "2016-01-01").medProperty("tom", "2016-01-02"));

        ReiseObligatoriskSamling samling = new SamlingReiseTilXml().transform(soknad);

        assertThat(samling.getPeriode().getFom()).is(periodeMatcher(2016, 1, 1));
        assertThat(samling.getPeriode().getTom()).is(periodeMatcher(2016, 1, 2));
    }

    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger").medValue("en"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.aktivitetsperiode")
                .medProperty("fom", "2016-01-01").medProperty("tom", "2016-01-02")
                .medProperty("utgiftoffentligtransport", "100"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling")
                .medProperty("fom", "2016-01-01").medProperty("tom", "2016-01-02")
                .medProperty("utgiftoffentligtransport", "100"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.fleresamlinger.samling")
                .medProperty("fom", "2016-01-01").medProperty("tom", "2016-01-02")
                .medProperty("utgiftoffentligtransport", "100"));


        soknad.getFakta().add(new Faktum().medKey("reise.samling.reisemaal").medProperty("adresse", "En adresse").medProperty("postnr", "2233"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.reiselengde").medValue("100"));

        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.parkering").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.sendebekreftelse").medValue(uke.name()));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.drosje").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.drosje.belop").medValue("50"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.kostnader.bompenger").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.kostnader.parkering").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.kostnader.piggdekk").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.kostnader.ferge").medValue("3"));
        soknad.getFakta().add(new Faktum().medKey("reise.samling.offentligtransport.egenbil.kostnader.annet").medValue("4"));


        ReiseObligatoriskSamling xml = new SamlingReiseTilXml().transform(soknad);

        assertThat(xml.getReiseadresser()).isEqualTo("En adresse, 2233");
        assertThat(xml.getAvstand()).isEqualTo(new BigInteger("100"));

        assertThat(xml.getAlternativeTransportutgifter().getDrosjeTransportutgifter().getBeloep().equals(new BigInteger("50")));
        assertThat(xml.getAlternativeTransportutgifter().getEgenBilTransportutgifter().getSumAndreUtgifter()).isEqualTo(11d);
        assertThat(xml.getAlternativeTransportutgifter().isKanEgenBilBrukes()).isEqualTo(true);
        assertThat(xml.getAlternativeTransportutgifter().isKanOffentligTransportBrukes()).isEqualTo(false);
        assertThat(xml.getAlternativeTransportutgifter().getKollektivTransportutgifter()).isEqualTo(null);
        soknad.getFaktumMedKey("reise.samling.offentligtransport").setValue("true");
        xml = new SamlingReiseTilXml().transform(soknad);
        assertThat(xml.getAlternativeTransportutgifter().getKollektivTransportutgifter().getBeloepPerMaaned()).isEqualTo(new BigInteger("100"));

        soknad.getFaktumMedKey("reise.samling.fleresamlinger").setValue("flere");
        xml = new SamlingReiseTilXml().transform(soknad);
        assertThat(xml.getAlternativeTransportutgifter().getKollektivTransportutgifter().getBeloepPerMaaned()).isEqualTo(new BigInteger("200"));
    }
}
