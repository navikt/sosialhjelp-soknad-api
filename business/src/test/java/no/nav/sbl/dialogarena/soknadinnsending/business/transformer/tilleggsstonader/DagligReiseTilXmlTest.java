package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DagligReise;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.DrosjeTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.EgenBilTransportutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Innsendingsintervaller;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.assertj.core.api.Condition;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader.StofoKodeverkVerdier.InnsendingsintervallerKodeverk.uke;
import static org.assertj.core.api.Assertions.assertThat;


public class DagligReiseTilXmlTest {


    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.periode").medProperty("fom", "2015-01-02").medProperty("tom", "2015-03-04"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.dagligreiseavstand").medValue("123"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.reisemaal").medProperty("land", "Spania").medProperty("adresse", "adresse").medProperty("postnr", "1256").medProperty("utenlandskadresse", "Sydengata"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.parkering").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.sendebekreftelse").medValue(uke.name()));

        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.drosje").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.drosje.belop").medValue("50"));

        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.kostnader.bompenger").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.kostnader.piggdekk").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.kostnader.ferge").medValue("3"));
        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.kostnader.annet").medValue("4"));

        soknad.getFakta().add(new Faktum().medKey("reise.aktivitet.offentligtransport.egenbil.parkering.belop").medValue("30"));

        DagligReise result = new DagligReiseTilXml().transform(soknad);

        assertThat(result.getAktivitetsadresse()).isEqualTo("Sydengata, Spania");
        assertThat(result.getAvstand()).isEqualTo(123d);
        assertThat(result.getPeriode().getFom()).is(new PeriodMatcher(2015, 1, 2));
        assertThat(result.getPeriode().getTom()).is(new PeriodMatcher(2015, 3, 4));

        assertThat(result.isHarParkeringsutgift()).isEqualTo(false);
        assertThat(result.getInnsendingsintervall()).is(new Condition<Innsendingsintervaller>() {
            @Override
            public boolean matches(Innsendingsintervaller value) {
                return value.getValue().equals(uke.kodeverksverdi);
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
                return value.getSumAndreUtgifter().equals(10d);
            }
        });

        assertThat(result.getAlternativeTransportutgifter().isKanEgenBilBrukes()).isEqualTo(true);
        assertThat(result.getAlternativeTransportutgifter().isKanOffentligTransportBrukes()).isEqualTo(false);

        assertThat(result.getParkeringsutgiftBeloep()).isEqualTo(new BigInteger("30"));
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