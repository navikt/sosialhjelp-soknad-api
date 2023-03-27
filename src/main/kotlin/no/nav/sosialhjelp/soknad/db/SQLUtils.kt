package no.nav.sosialhjelp.soknad.db

import java.sql.Timestamp
import java.time.LocalDateTime

object SQLUtils {

    private const val HSQLDB = "hsqldb"
    private const val POSTGRESQL = "postgresql"
    const val DIALECT_PROPERTY = "sqldialect"

    fun limit(limit: Int): String {
        return when (System.getProperty(DIALECT_PROPERTY)) {
            HSQLDB, POSTGRESQL -> "limit $limit"
            else -> "and rownum <= $limit"
        }
    }

    fun selectNextSequenceValue(sequence: String): String {
        return when (System.getProperty(DIALECT_PROPERTY)) {
            HSQLDB -> "call next value for $sequence"
            POSTGRESQL -> "select nextval('$sequence')"
            else -> "select $sequence.nextval from dual"
        }
    }

    fun tidTilTimestamp(tid: LocalDateTime?): Timestamp? {
        return tid?.let { Timestamp.valueOf(it) }
    }

    fun nullableTimestampTilTid(timestamp: Timestamp?): LocalDateTime? {
        return timestamp?.toLocalDateTime()
    }

    fun timestampTilTid(timestamp: Timestamp): LocalDateTime {
        return timestamp.toLocalDateTime()
    }

    fun blobSizeQuery(): String {
        return when (System.getProperty(DIALECT_PROPERTY)) {
            HSQLDB, POSTGRESQL -> "OCTET_LENGTH(DATA)"
            else -> "dbms_lob.getLength(DATA)"
        }
    }
}
