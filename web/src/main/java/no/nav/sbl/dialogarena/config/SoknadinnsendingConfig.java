package no.nav.sbl.dialogarena.config;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl; //NOSONAR
import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import org.joda.time.DateTime;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXB;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.GregorianCalendar;

@EnableAspectJAutoProxy
@Configuration
@Import({
        ApplicationConfig.class,
        BusinessConfig.class,
        CacheConfig.class,
        GAConfig.class,
        ConsumerConfig.class,
        ContentConfig.class,
        SoknadInnsendingDBConfig.class,
        HandlebarsHelperConfig.class,
        MetricsConfig.class
})
@ComponentScan(basePackages = "no.nav.sbl.dialogarena.rest")
public class SoknadinnsendingConfig {

    @PostConstruct
    public void ensureThatXmlMarshalingWorks() throws DatatypeConfigurationException {
        marshalTest1();
        marshalTest2();
    }

    private void marshalTest1() throws DatatypeConfigurationException {
        GregorianCalendar inputDate = DateTime.parse("2017-05-15T15:15+01:00").toGregorianCalendar();

        XMLGregorianCalendar dataTypeFactoryDato = DatatypeFactory.newInstance().newXMLGregorianCalendar(inputDate);
        XMLGregorianCalendarImpl sunInternalDato = new XMLGregorianCalendarImpl(inputDate);
        ByteArrayOutputStream dateTypeFactoryDatoMarshalledBytes = new ByteArrayOutputStream();
        JAXB.marshal(dataTypeFactoryDato, dateTypeFactoryDatoMarshalledBytes);
        ByteArrayOutputStream sunInternalDatoMarshalledBytes = new ByteArrayOutputStream();
        JAXB.marshal(sunInternalDato, sunInternalDatoMarshalledBytes);

        boolean xmlFormatsAreEqual = dataTypeFactoryDato.toXMLFormat().equals(sunInternalDato.toXMLFormat());
        boolean marshalledBytesAreEqual = Arrays.equals(dateTypeFactoryDatoMarshalledBytes.toByteArray(), sunInternalDatoMarshalledBytes.toByteArray());
        Assert.isTrue(xmlFormatsAreEqual);
        Assert.isTrue(marshalledBytesAreEqual);
    }

    private void marshalTest2() throws DatatypeConfigurationException {
        GregorianCalendar forventetDato2 = DateTime.parse("2014-12-31T23:59").toGregorianCalendar();
        XMLGregorianCalendar dataTypeFactoryDato = DatatypeFactory.newInstance().newXMLGregorianCalendar(forventetDato2);
        XMLGregorianCalendarImpl sunInternalDato = new XMLGregorianCalendarImpl(forventetDato2);
        ByteArrayOutputStream dateTypeFactoryDatoMarshalledBytes = new ByteArrayOutputStream();
        JAXB.marshal(dataTypeFactoryDato, dateTypeFactoryDatoMarshalledBytes);
        ByteArrayOutputStream sunInternalDatoMarshalledBytes = new ByteArrayOutputStream();
        JAXB.marshal(sunInternalDato, sunInternalDatoMarshalledBytes);
        boolean xmlFormatsAreEqual = dataTypeFactoryDato.toXMLFormat().equals(sunInternalDato.toXMLFormat());
        boolean marshalledBytesAreEqual = Arrays.equals(dateTypeFactoryDatoMarshalledBytes.toByteArray(), sunInternalDatoMarshalledBytes.toByteArray());
        Assert.isTrue(xmlFormatsAreEqual);
        Assert.isTrue(marshalledBytesAreEqual);
    }

}