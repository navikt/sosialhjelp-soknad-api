package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class ServiceUtils {
    public static String datoTilString(LocalDate date) {
        return date != null ? date.toString("yyyy-MM-dd") : "";
    }

    public static XMLGregorianCalendar stringTilXmldato(String dato) {
        return lagDatatypeFactory().newXMLGregorianCalendar(DateTime.parse(dato).toGregorianCalendar());
    }

    public static String nullToBlank(Object value) {
        if (value != null) {
            return value.toString();
        }
        return "";
    }

    public static DatatypeFactory lagDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
