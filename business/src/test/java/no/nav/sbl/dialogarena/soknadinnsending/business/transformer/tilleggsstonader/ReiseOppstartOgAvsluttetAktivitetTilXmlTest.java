package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseOppstartOgAvsluttetAktivitet;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.ReiseOppstartOgAvsluttetAktivitetTilXml;
import org.assertj.core.api.Condition;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class ReiseOppstartOgAvsluttetAktivitetTilXmlTest {
    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.periode").medProperty("fom", "2015-01-02").medProperty("tom", "2015-03-04"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.reiselengde").medValue("123"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.antallreiser").medValue("77"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.reisemaal").medProperty("adresse", "adresse").medProperty("postnr", "1256"));

        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.hjemmeboende").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("barn").medProperty("skalFlytteMed", "true"));

        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.drosje").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.drosje.belop").medValue("50"));

        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport").medValue("false"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil").medValue("true"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.bompenger").medValue("1"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.parkering").medValue("1"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.piggdekk").medValue("2"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.ferge").medValue("3"));
        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.kostnader.annet").medValue("4"));

        soknad.medFaktum(new Faktum().medKey("reise.midlertidig.offentligtransport.egenbil.parkering.belop").medValue("30"));

        ReiseOppstartOgAvsluttetAktivitet result = new ReiseOppstartOgAvsluttetAktivitetTilXml().transform(soknad);

        assertThat(result.getAktivitetsstedAdresse()).isEqualTo("adresse, 1256");
        assertThat(result.getAvstand()).isEqualTo(new BigInteger("123"));
        assertThat(result.getAntallReiser()).isEqualTo(new BigInteger("77"));
        assertThat(result.isHarBarnUnderAtten()).isEqualTo(true);
        assertThat(result.getPeriode().getFom()).is(new PeriodMatcher(2015, 1, 2));
        assertThat(result.getPeriode().getTom()).is(new PeriodMatcher(2015, 3, 4));
        assertThat(result.isHarBarnUnderFemteklasse()).isEqualTo(true);

        assertThat(result.getAlternativeTransportutgifter().getDrosjeTransportutgifter().getBeloep()).isEqualTo(new BigInteger("50"));
        assertThat(result.getAlternativeTransportutgifter().getEgenBilTransportutgifter().getSumAndreUtgifter()).isEqualTo(11d);
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