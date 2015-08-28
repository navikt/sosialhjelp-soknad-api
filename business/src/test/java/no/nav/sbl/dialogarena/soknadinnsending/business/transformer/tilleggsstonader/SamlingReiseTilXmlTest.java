package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseObligatoriskSamling;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Test;

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

}
