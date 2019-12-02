package no.nav.sbl.dialogarena.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NedetidUtils {
    private static final Logger log = LoggerFactory.getLogger(NedetidUtils.class);
    private final static int planlagtNedetidVarselAntallDager = 14;
    public final static String NEDETID_START = "nedetid.start";
    public final static String NEDETID_SLUTT = "nedetid.slutt";
    public final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static LocalDateTime getNedetid(String propertyname) {
        String nedetid = System.getProperty(propertyname, null);
        if (nedetid == null) return null;

        try {
            return LocalDateTime.parse(nedetid, dateFormat);
        } catch (DateTimeParseException e) {
            log.error("Klarte ikke parse {}: {}. Skal være på formatet: {}", propertyname, nedetid, dateFormat.toString());
            return null;
        }
    }

    public static String getNedetidAsStringOrNull(String propertyname) {
        LocalDateTime nedetid = getNedetid(propertyname);
        return nedetid == null ? null : nedetid.format(dateFormat);
    }

    public static boolean isInnenforPlanlagtNedetid() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = getNedetid(NEDETID_START);
        LocalDateTime slutt = getNedetid(NEDETID_SLUTT);

        if (start == null || slutt == null || slutt.isBefore(start)) return false;
        return now.plusDays(planlagtNedetidVarselAntallDager).isAfter(start) && now.isBefore(start);
    }

    public static boolean isInnenforNedetid() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = getNedetid(NEDETID_START);
        LocalDateTime slutt = getNedetid(NEDETID_SLUTT);

        if (start == null || slutt == null || slutt.isBefore(start)) return false;
        return now.isAfter(start) && now.isBefore(slutt);
    }
}
