package no.nav.sbl.dialogarena.sendsoknad.domain;


import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.io.ByteArrayOutputStream;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;

public class FaktumTest {
    private Faktum faktum;
    private DatatypeFactory datatypeFactory;

    @Before
    public void setup() {
        faktum = lagFaktum();
        datatypeFactory = lagDatatypeFactory();
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
        GregorianCalendar forventetDato1 = DateTime.parse("2017-05-15T15:15+01:00").toGregorianCalendar();
        XMLGregorianCalendar testdato1 = datatypeFactory.newXMLGregorianCalendar(forventetDato1);
        XMLGregorianCalendarImpl testdato2 = new XMLGregorianCalendarImpl(forventetDato1);
        ByteArrayOutputStream xmlTestdato1 = new ByteArrayOutputStream();
        JAXB.marshal(testdato1, xmlTestdato1);
        ByteArrayOutputStream xmlTestdato2 = new ByteArrayOutputStream();
        JAXB.marshal(testdato2, xmlTestdato2);

        GregorianCalendar forventetDato2 = DateTime.parse("2014-12-31T23:59").toGregorianCalendar();
        XMLGregorianCalendar testdato3 = datatypeFactory.newXMLGregorianCalendar(forventetDato2);
        XMLGregorianCalendarImpl testdato4 = new XMLGregorianCalendarImpl(forventetDato2);
        ByteArrayOutputStream xmlTestdato3 = new ByteArrayOutputStream();
        JAXB.marshal(testdato3, xmlTestdato3);
        ByteArrayOutputStream xmlTestdato4 = new ByteArrayOutputStream();
        JAXB.marshal(testdato4, xmlTestdato4);

        assertThat(testdato1.toXMLFormat(), is(testdato2.toXMLFormat()));
        assertThat(testdato3.toXMLFormat(), is(testdato4.toXMLFormat()));
        assertThat(testdato1.getXMLSchemaType(), is(testdato2.getXMLSchemaType()));
        assertThat(xmlTestdato1.toByteArray(), is(xmlTestdato2.toByteArray()));
        assertThat(xmlTestdato3.toByteArray(), is(xmlTestdato4.toByteArray()));
    }
}
