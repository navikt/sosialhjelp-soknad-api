package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ErUtgifterDekket;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Laeremiddelutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Skolenivaaer;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.SkolenivaaerKodeverk;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.ErUtgifterDekketKodeverk;
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
        assertThat(xml.getErUtgifterDekket()).is(new Condition<ErUtgifterDekket>() {

            @Override
            public boolean matches(ErUtgifterDekket value) {
                as("ErUtgifterDekket<%s>", ErUtgifterDekketKodeverk.ja.kodeverk);
                return value.getValue().equals(ErUtgifterDekketKodeverk.ja.kodeverk);
            }
        });
        assertThat(xml.getPeriode().getFom()).is(periodeMatcher(2015, 1, 1));
        assertThat(xml.getPeriode().getTom()).is(periodeMatcher(2015, 2, 1));
        assertThat(xml.getProsentandelForUtdanning()).isEqualTo(new BigInteger("50"));
        assertThat(xml.getSkolenivaa()).is(new Condition<Skolenivaaer>() {

            @Override
            public boolean matches(Skolenivaaer value) {
                as("Skolenivaaer<%s>",SkolenivaaerKodeverk.videregaende.kodeverk);
                return value.getValue().equals(SkolenivaaerKodeverk.videregaende.kodeverk);
            }
        });
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

        assertThat(xml.getErUtgifterDekket()).is(new Condition<ErUtgifterDekket>() {

            @Override
            public boolean matches(ErUtgifterDekket value) {
                as("ErUtgifterDekket<%s>", ErUtgifterDekketKodeverk.nei.kodeverk);
                return value.getValue().equals(ErUtgifterDekketKodeverk.nei.kodeverk);
            }
        });
    }

    @Test
    public void skalKonvertereFaktumStrukturForLaeremiddelDelvisDekket() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.dekket").medValue("delvis"));

        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getErUtgifterDekket()).is(new Condition<ErUtgifterDekket>() {

            @Override
            public boolean matches(ErUtgifterDekket value) {
                as("ErUtgifterDekket<%s>", ErUtgifterDekketKodeverk.delvis.kodeverk);
                return value.getValue().equals(ErUtgifterDekketKodeverk.delvis.kodeverk);
            }
        });
    }

    @Test
    public void skalKonvertereFaktumStrukturForSkolenivaHoyreutdanning() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.utdanningstype").medValue("hoyereutdanning"));
        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getSkolenivaa()).is(new Condition<Skolenivaaer>() {

            @Override
            public boolean matches(Skolenivaaer value) {
                as("Skolenivaaer<%s>",SkolenivaaerKodeverk.hoyereutdanning.kodeverk);
                return value.getValue().equals(SkolenivaaerKodeverk.hoyereutdanning.kodeverk);
            }
        });
    }

    @Test
    public void skalKonvertereFaktumStrukturForSkolenivaAnnet() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("laeremidler.utdanningstype").medValue("annet"));
        Laeremiddelutgifter xml = new LaeremidlerTilXml().transform(soknad);

        assertThat(xml.getSkolenivaa()).is(new Condition<Skolenivaaer>() {

            @Override
            public boolean matches(Skolenivaaer value) {
                as("Skolenivaaer<%s>",SkolenivaaerKodeverk.annet.kodeverk);
                return value.getValue().equals(SkolenivaaerKodeverk.annet.kodeverk);
            }
        });
    }
}
