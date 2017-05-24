package no.nav.sbl.dialogarena.sendsoknad.domain;


import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;

public class FaktumTest {
    private Faktum faktum;

    @Before
    public void setup() {
        faktum = lagFaktum();
    }

    private Faktum lagFaktum() {
        return new Faktum()
                .medKey("nokkel")
                .medType(Faktum.FaktumType.BRUKERREGISTRERT)
                .medProperty("key1", "value1")
                .medProperty("key2", "value2")
                .medSystemProperty("system1", "value2")
                .medSystemProperty("system2", "value2");
    }

    @Test
    public void skalKorrektHenteTypeString(){
        assertThat(lagFaktum().getTypeString(), is(Faktum.FaktumType.BRUKERREGISTRERT.name()));
    }
    @Test
    public void skalHaEgenskap() {
        assertThat(faktum.hasEgenskap("key1"), is(true));
        assertThat(faktum.hasEgenskap("key2"), is(true));
        assertThat(faktum.hasEgenskap("ikkeekstisterende"), is(false));
    }

    @Test
    public void skalKopiereSystemlagrede(){
        Faktum sysFaktum = lagFaktum();
        sysFaktum.finnEgenskap("system1").setValue("ikkeEndret");
        sysFaktum.finnEgenskap("system2").setValue("ikkeEndret");
        faktum.medSystemProperty("system3", "value3");
        faktum.kopierSystemlagrede(sysFaktum);
        assertThat(faktum.finnEgenskap("system3"), is(nullValue()));
        assertThat(faktum.finnEgenskap("system1").getValue(), is("ikkeEndret"));
        assertThat(faktum.finnEgenskap("system2").getValue(), is("ikkeEndret"));
        assertThat(faktum.finnEgenskap("key1").getValue(), is("value1"));
        assertThat(faktum.finnEgenskap("key2").getValue(), is("value2"));
    }
    @Test
    public void skalKopiereBrukerlagrede(){
        Faktum faktum = new Faktum().medSystemProperty("system1", "sysVerdi1");
        faktum.kopierFaktumegenskaper(lagFaktum());
        assertThat(faktum.finnEgenskap("system1").getValue(), is("sysVerdi1"));
        assertThat(faktum.finnEgenskap("system2"), is(nullValue()));
        assertThat(faktum.finnEgenskap("key1").getValue(), is("value1"));
        assertThat(faktum.finnEgenskap("key2").getValue(), is("value2"));
    }

    @Test
    public void skalKonvertereDatostrengTilXML() {
        GregorianCalendar forventetDato1 = DateTime.parse("2017-01-01T13:27").toGregorianCalendar();
        XMLGregorianCalendar testdato1 = lagDatatypeFactory().newXMLGregorianCalendar(forventetDato1);
        XMLGregorianCalendarImpl testdato2 = new XMLGregorianCalendarImpl(forventetDato1);

        GregorianCalendar forventetDato2 = DateTime.parse("2014-12-31T00:00").toGregorianCalendar();
        XMLGregorianCalendar testdato3 = lagDatatypeFactory().newXMLGregorianCalendar(forventetDato2);
        XMLGregorianCalendarImpl testdato4 = new XMLGregorianCalendarImpl(forventetDato2);

        assertThat(testdato1.toString(), is(testdato2.toString()));
        assertThat(testdato1.toXMLFormat(), is(testdato2.toXMLFormat()));
        assertThat(testdato3.toString(), is(testdato4.toString()));
        assertThat(testdato3.toXMLFormat(), is(testdato4.toXMLFormat()));
        assertThat(testdato1.getXMLSchemaType(), is(testdato2.getXMLSchemaType()));
    }
}
