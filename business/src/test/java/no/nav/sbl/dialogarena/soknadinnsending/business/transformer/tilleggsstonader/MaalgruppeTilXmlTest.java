package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Maalgruppeinformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.datatype.XMLGregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MaalgruppeTilXmlTest {

    private MaalgruppeTilXml tilXml;
    private Faktum maalgruppeFaktum;

    @Before
    public void beforeEach() {
        tilXml = new MaalgruppeTilXml();
        maalgruppeFaktum = new Faktum().medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medProperty("kodeverkVerdi", "ARBSOKERE")
                .medProperty("fom", "2015-01-01")
                .medProperty("tom", "2015-02-02");
    }

    @Test
    public void setterKodeverkRefFraFaktum() {
        Maalgruppeinformasjon maalgruppeinformasjon = tilXml.transform(maalgruppeFaktum);
        assertThat(maalgruppeinformasjon.getMaalgruppetype().getKodeverksRef()).isEqualTo("ARBSOKERE");
    }

    @Test
    public void setterPeriodeFraFaktum() {
        Maalgruppeinformasjon maalgruppeinformasjon = tilXml.transform(maalgruppeFaktum);

        XMLGregorianCalendar fom = maalgruppeinformasjon.getPeriode().getFom();
        assertThat(fom.getDay()).isEqualTo(1);
        assertThat(fom.getMonth()).isEqualTo(1);
        assertThat(fom.getYear()).isEqualTo(2015);

        XMLGregorianCalendar tom = maalgruppeinformasjon.getPeriode().getTom();
        assertThat(tom.getDay()).isEqualTo(2);
        assertThat(tom.getMonth()).isEqualTo(2);
        assertThat(tom.getYear()).isEqualTo(2015);
    }

    @Test
    public void tilDatoErIkkeObligatorisk() {
        Maalgruppeinformasjon maalgruppeinformasjon = tilXml.transform(maalgruppeFaktum.medProperty("tom", null));
        XMLGregorianCalendar tom = maalgruppeinformasjon.getPeriode().getTom();
        assertThat(tom).isNull();
    }

    @Test
    public void tomPeriode() {
        Maalgruppeinformasjon maalgruppeinformasjon = tilXml.transform(maalgruppeFaktum.medProperty("fom", null).medProperty("tom", null));
        assertThat(maalgruppeinformasjon.getPeriode()).isNull();
    }
}