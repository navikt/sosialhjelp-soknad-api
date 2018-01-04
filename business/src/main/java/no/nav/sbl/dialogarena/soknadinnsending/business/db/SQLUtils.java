package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import static java.lang.System.getProperty;

public class SQLUtils {
	
	public static final String DIALECT_PROPERTY = "sqldialect";

	public static String limit(int limit) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "limit " + limit;
		} else {
			return "and rownum <= " + limit;
		}
	}

	public static String whereLimit(int limit) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "limit " + limit;
		} else {
			return "where rownum <= " + limit;
		}
	}

	public static String toDate(int antallDager) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "CURRENT_TIMESTAMP - " + antallDager + " DAY";
		} else {
			return "CURRENT_TIMESTAMP - NUMTODSINTERVAL("+antallDager+",'DAY') " ;
		}
	}

	public static String selectNextSequenceValue(String sequence) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "call next value for " + sequence;
		} else {
			return "select " + sequence + ".nextval from dual";
		}
	}

	public static String selectMultipleNextSequenceValues(String sequence) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "select next value for " + sequence + " from unnest(sequence_array(1,?,1))";
		} else {
			return "select " + sequence + ".nextval from dual connect by level <= ?";
		}
	}

}
