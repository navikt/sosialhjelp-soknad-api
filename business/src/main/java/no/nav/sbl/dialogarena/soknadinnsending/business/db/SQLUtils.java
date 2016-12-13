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
