package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.xml.datatype.XMLGregorianCalendar;

public class ServiceUtils {
    public static String datoTilString(LocalDate date) {
        return date != null ? date.toString("yyyy-MM-dd") : "";
    }

    public static XMLGregorianCalendar stringTilXmldato(String dato) {
        return new XMLGregorianCalendarImpl(DateTime.parse(dato).toGregorianCalendar());
    }

    public static String nullToBlank(Object value) {
        if (value != null) {
            return value.toString();
        }
        return "";
    }
}
