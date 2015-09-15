package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseOppstartOgAvsluttetAktivitet;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.assertj.core.api.Condition;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ReiseOppstartOgAvsluttetAktivitetTilXmlTest {
    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.periode").medProperty("fom", "2015-01-02").medProperty("tom", "2015-03-04"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.reiselengde").medValue("123"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.reisemaal").medProperty("adresse", "adresse").medProperty("postnr", "1256"));

        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.hjemmeboende").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("barn").medProperty("skalFlytteMed", "true"));

        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.drosje").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.drosje.belop").medValue("50"));

        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.bompenger").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.parkering").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.piggdekk").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.ferge").medValue("3"));
        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.annet").medValue("4"));

        soknad.getFakta().add(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.parkering.belop").medValue("30"));

        ReiseOppstartOgAvsluttetAktivitet result = new ReiseOppstartOgAvsluttetAktivitetTilXml().transform(soknad);

        assertThat(result.getAktivitetsstedAdresse()).isEqualTo("adresse, 1256");
        assertThat(result.getAvstand()).isEqualTo(new BigInteger("123"));
        assertThat(result.isHarBarnUnderAtten()).isEqualTo(true);
        assertThat(result.getPeriode().getFom()).is(new PeriodMatcher(2015, 1, 2));
        assertThat(result.getPeriode().getTom()).is(new PeriodMatcher(2015, 3, 4));
        assertThat(result.isHarBarnUnderFemteklasse()).isEqualTo(true);


        assertThat(result.getAlternativeTransportutgifter().getDrosjeTransportutgifter()).is(new Condition<DrosjeTransportutgifter>() {
            @Override
            public boolean matches(DrosjeTransportutgifter value) {
                return value.getBeloep().equals(new BigInteger("50"));
            }
        });

        assertThat(result.getAlternativeTransportutgifter().getEgenBilTransportutgifter()).is(new Condition<EgenBilTransportutgifter>() {
            @Override
            public boolean matches(EgenBilTransportutgifter value) {
                return value.getSumAndreUtgifter().equals(11d);
            }
        });

        assertThat(result.getAlternativeTransportutgifter().isKanEgenBilBrukes()).isEqualTo(true);
        assertThat(result.getAlternativeTransportutgifter().isKanOffentligTransportBrukes()).isEqualTo(false);

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