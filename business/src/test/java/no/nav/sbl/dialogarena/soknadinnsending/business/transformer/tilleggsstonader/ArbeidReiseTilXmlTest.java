package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.FormaalKodeverk.oppfolging;
import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.assertj.core.api.Assertions.assertThat;

public class ArbeidReiseTilXmlTest {
    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.registrert").medValue("2015-01-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.hvorforreise").medValue(oppfolging.toString()));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reiselengde").medValue("123"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reisemaal").medProperty("land", "Norge").medProperty("adresse", "adresse").medProperty("postnr", "1256").medProperty("utenlandskadresse", "syden"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reisedekket").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.dagpenger.forlenget").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.dagpenger.bortfall").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.dagpenger").medValue("true"));

        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.drosje").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.drosje.belop").medValue("50"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.utgift").medValue("123"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.bompenger").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.parkering").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.piggdekk").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.ferge").medValue("3"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.annet").medValue("4"));

        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.parkering.belop").medValue("30"));

        ReisestoenadForArbeidssoeker result = new ArbeidReiseTilXml().transform(soknad);

        assertThat(result.getAdresse()).isEqualTo("adresse, 1256");
        assertThat(result.getAvstand()).isEqualTo(new BigInteger("123"));
        assertThat(result.getReisedato()).is(periodeMatcher(2015, 1, 2));
        assertThat(result.isErUtgifterDekketAvAndre()).isEqualTo(true);
        assertThat(result.isErVentetidForlenget()).isEqualTo(true);
        assertThat(result.isFinnesTidsbegrensetbortfall()).isEqualTo(true);
        assertThat(result.isHarMottattDagpengerSisteSeksMaaneder()).isEqualTo(true);
        assertThat(result.getFormaal()).is(new Condition<Formaal>() {

            @Override
            public boolean matches(Formaal value) {
                as("Formal<%s>", oppfolging.kodeverksverdi);
                return value.getValue().equals(oppfolging.kodeverksverdi);
            }
        });
        assertThat(result.getAlternativeTransportutgifter().getDrosjeTransportutgifter()).is(new Condition<DrosjeTransportutgifter>() {
            @Override
            public boolean matches(DrosjeTransportutgifter value) {
                return value.getBeloep().equals(new BigInteger("50"));
            }
        });

        assertThat(result.getAlternativeTransportutgifter().getEgenBilTransportutgifter()).is(new Condition<EgenBilTransportutgifter>() {
            @Override
            public boolean matches(EgenBilTransportutgifter value) {
                return value.getSumAndreUtgifter().equals(12d);
            }
        });

        assertThat(result.getAlternativeTransportutgifter().isKanEgenBilBrukes()).isEqualTo(true);
        assertThat(result.getAlternativeTransportutgifter().isKanOffentligTransportBrukes()).isEqualTo(false);
        assertThat(result.getAlternativeTransportutgifter().getKollektivTransportutgifter()).isNull();
        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport").medValue("true");
        result = new ArbeidReiseTilXml().transform(soknad);
        assertThat(result.getAlternativeTransportutgifter().getKollektivTransportutgifter()).is(new Condition<KollektivTransportutgifter>() {
            @Override
            public boolean matches(KollektivTransportutgifter value) {
                as("KollektivTransportutgifter<%s>", "123");
                return value.getBeloepPerMaaned().equals(new BigInteger("123"));
            }
        });

    }



}