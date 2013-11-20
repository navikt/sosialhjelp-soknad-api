package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import static java.lang.System.getProperty;

/**
 * Hjelpeklasse for å håndtere forskjeller mellom sqldialekter
 */
public class SQLUtils {
	
	public static final String DIALECT_PROPERTY = "sqldialect";

	public static String limit(int limit) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "limit " + limit;
		} else {
			return "and rownum <= " + limit;
		}
	}
	
	public static String nextvalue(String sequence) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "next value for " + sequence;
		} else {
			return sequence + ".nextval";
		}
	}
	
	public static String selectNextSequenceValue(String sequence) {
		if ("hsqldb".equals(getProperty(DIALECT_PROPERTY))) {
			return "call next value for " + sequence;
		} else {
			return "select " + sequence + ".nextval from dual";
		}
	}

}
