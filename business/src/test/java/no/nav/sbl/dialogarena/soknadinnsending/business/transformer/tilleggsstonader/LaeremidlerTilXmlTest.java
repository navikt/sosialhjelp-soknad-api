package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Laeremiddelutgifter;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.LaeremidlerTilXml;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier;
import org.junit.Test;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.assertj.core.api.Assertions.assertThat;

public class LaeremidlerTilXmlTest {

    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.funksjonshemming").medValue("true").medProperty("utgift", "100"));
        soknad.getFakta().add(new Faktum().medKey("laeremidler.dekket").medValue("ja"));
        soknad.getFakta().add(new Faktum().medKey("laeremidler.periode").medProperty("fom", "2015-01-01").medProperty("tom", "2015-02-01"));
        soknad.getFakta().add(new Faktum().medKey("laeremidler.deltidsstudent").medValue("50"));
        soknad.getFakta().add(new Faktum().medKey("laeremidler.utdanningstype").medValue("videregaende"));

        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getBeloep()).isEqualTo(new BigInteger("100"));
        assertThat(xml.getBeloep()).isEqualTo(new BigInteger("100"));
        assertThat(xml.getErUtgifterDekket().getValue()).isEqualTo(StofoKodeverkVerdier.ErUtgifterDekketKodeverk.ja.kodeverk);
        assertThat(xml.getPeriode().getFom()).is(periodeMatcher(2015, 1, 1));
        assertThat(xml.getPeriode().getTom()).is(periodeMatcher(2015, 2, 1));
        assertThat(xml.getProsentandelForUtdanning()).isEqualTo(new BigInteger("50"));
        assertThat(xml.getSkolenivaa().getValue()).isEqualTo(StofoKodeverkVerdier.SkolenivaaerKodeverk.videregaende.kodeverk);
    }

    @Test
    public void skalKonvertereFaktumStrukturUtenUtgiftForFunksjonshemming() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.funksjonshemming").medValue("false").medProperty("utgift", "100"));
        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getBeloep()).isNotEqualTo(new BigInteger("100"));
    }

    @Test
    public void skalKonvertereFaktumStrukturForLaeremiddelIkkeDekket() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.dekket").medValue("nei"));
        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getErUtgifterDekket().getValue()).isEqualTo(StofoKodeverkVerdier.ErUtgifterDekketKodeverk.nei.kodeverk);
    }

    @Test
    public void skalKonvertereFaktumStrukturForLaeremiddelDelvisDekket() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.dekket").medValue("delvis"));

        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getErUtgifterDekket().getValue()).isEqualTo(StofoKodeverkVerdier.ErUtgifterDekketKodeverk.delvis.kodeverk);
    }

    @Test
    public void skalKonvertereFaktumStrukturForSkolenivaHoyreutdanning() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.utdanningstype").medValue("hoyereutdanning"));
        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getSkolenivaa().getValue()).isEqualTo(StofoKodeverkVerdier.SkolenivaaerKodeverk.hoyereutdanning.kodeverk);
    }

    @Test
    public void skalKonvertereFaktumStrukturForSkolenivaAnnet() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.utdanningstype").medValue("annet"));
        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getSkolenivaa().getValue()).isEqualTo(StofoKodeverkVerdier.SkolenivaaerKodeverk.annet.kodeverk);
    }
}
