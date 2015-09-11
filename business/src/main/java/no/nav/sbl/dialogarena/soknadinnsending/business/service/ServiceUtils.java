package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.joda.time.LocalDate;

public class ServiceUtils {
    static String datoTilString(LocalDate date) {
        return date != null ? date.toString("yyyy-MM-dd") : "";
    }

    static String nullToBlank(Object value) {
        if (value != null) {
            return value.toString();
        }
        return "";
    }
}
