package no.nav.sbl.dialogarena.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class NedetidUtils {
    private static final Logger log = LoggerFactory.getLogger(NedetidUtils.class);
    private final static int planlagtNedetidVarselAntallDager = 14;
    public final static String NEDETID_START = "nedetid.start";
    public final static String NEDETID_SLUTT = "nedetid.slutt";
    public final static String nedetidFormat = "dd.MM.yyyy HH:mm:ss";
    public final static String humanreadableFormat = "EEEE dd.MM.yyyy 'kl.' HH:mm";
    public final static String humanreadableFormatEn = "EEEE d MMM yyyy 'at' HH:mm '(CET)'";
    public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(nedetidFormat);
    public final static DateTimeFormatter humanreadableFormatter = DateTimeFormatter.ofPattern(humanreadableFormat,  new Locale("nb", "NO"));
    public final static DateTimeFormatter humanreadableFormatterEn = DateTimeFormatter.ofPattern(humanreadableFormatEn,  Locale.ENGLISH);

    private static LocalDateTime getNedetid(String propertyname) {
        String nedetid = System.getProperty(propertyname, null);
        if (nedetid == null || nedetid.isEmpty()) return null;

        try {
            return LocalDateTime.parse(nedetid, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            log.error("Klarte ikke parse {}: {}. Skal være på formatet: {}", propertyname, nedetid, nedetidFormat);
            return null;
        }
    }

    public static String getNedetidAsStringOrNull(String propertyname) {
        return getNedetidAsFormattedStringOrNull(propertyname, dateTimeFormatter);
    }

    public static String getNedetidAsHumanReadable(String propertyname) {
        return getNedetidAsFormattedStringOrNull(propertyname, humanreadableFormatter);
    }

    public static String getNedetidAsHumanReadableEn(String propertyname) {
        return getNedetidAsFormattedStringOrNull(propertyname, humanreadableFormatterEn);
    }
    private static String getNedetidAsFormattedStringOrNull(String propertyname, DateTimeFormatter formatter) {
        LocalDateTime nedetid = getNedetid(propertyname);
        return nedetid == null ? null : nedetid.format(formatter);
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
