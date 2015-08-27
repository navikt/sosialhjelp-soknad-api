package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DagligReise;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Formaal;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Innsendingsintervaller;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.KollektivTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReisestoenadForArbeidssoeker;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.assertj.core.api.Condition;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class ArbeidReiseTilXmlTest {
    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.registrert").medValue("2015-01-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.hvorforreise").medValue("oppfolging"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reiselengde").medValue("123"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reisemaal").medProperty("adresse", "adresse").medProperty("postnr", "1256"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reisedekket").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.dagpenger.forlenget").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.dagpenger.bortfall").medValue("true"));

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
        assertThat(result.getReisedato()).is(new PeriodMatcher(2015, 1, 2));
        assertThat(result.isErUtgifterDekketAvAndre()).isEqualTo(true);
        assertThat(result.isErVentetidForlenget()).isEqualTo(true);
        assertThat(result.isFinnesTidsbegrensetbortfall()).isEqualTo(true);
        assertThat(result.getFormaal()).is(new Condition<Formaal>() {

            @Override
            public boolean matches(Formaal value) {
                as("Formal<%s>", "oppfolging");
                return value.getValue().equals("oppfolging");
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
        assertThat(result.getAlternativeTransportutgifter().getKollektivTransportutgifter()).is(new Condition<KollektivTransportutgifter>() {
            @Override
            public boolean matches(KollektivTransportutgifter value) {
                as("KollektivTransportutgifter<%s>", "123");
                return value.getBeloepPerMaaned().equals(new BigInteger("123"));
            }
        });

    }

    private static class PeriodMatcher extends Condition<XMLGregorianCalendar> {
        private int year, month, day;

        public PeriodMatcher(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @Override
        public boolean matches(XMLGregorianCalendar value) {
            return value.getYear() == year && value.getMonth() == month && value.getDay() == day;
        }
    }

}