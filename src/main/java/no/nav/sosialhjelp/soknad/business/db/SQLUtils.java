package no.nav.sosialhjelp.soknad.business.db;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static java.lang.System.getProperty;

public final class SQLUtils {

    private SQLUtils() {
    }

    private static final String HSQLDB = "hsqldb";
    public static final String DIALECT_PROPERTY = "sqldialect";

    public static String limit(int limit) {
        if (HSQLDB.equals(getProperty(DIALECT_PROPERTY))) {
            return "limit " + limit;
        } else {
            return "and rownum <= " + limit;
        }
    }

    public static String whereLimit(int limit) {
        if (HSQLDB.equals(getProperty(DIALECT_PROPERTY))) {
            return "limit " + limit;
        } else {
            return "where rownum <= " + limit;
        }
    }

    public static String toDate(int antallDager) {
        if (HSQLDB.equals(getProperty(DIALECT_PROPERTY))) {
            return "CURRENT_TIMESTAMP - " + antallDager + " DAY";
        } else {
            return "CURRENT_TIMESTAMP - NUMTODSINTERVAL("+antallDager+",'DAY') " ;
        }
    }

    public static String selectNextSequenceValue(String sequence) {
        if (HSQLDB.equals(getProperty(DIALECT_PROPERTY))) {
            return "call next value for " + sequence;
        } else {
            return "select " + sequence + ".nextval from dual";
        }
    }

    public static String selectMultipleNextSequenceValues(String sequence) {
        if (HSQLDB.equals(getProperty(DIALECT_PROPERTY))) {
            return "select next value for " + sequence + " from unnest(sequence_array(1,?,1))";
        } else {
            return "select " + sequence + ".nextval from dual connect by level <= ?";
        }
    }

    public static Timestamp tidTilTimestamp(LocalDateTime tid) {
        return tid != null
                ? Timestamp.valueOf(tid)
                : null;
    }

    public static LocalDateTime timestampTilTid(Timestamp timestamp) {
        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }
}
