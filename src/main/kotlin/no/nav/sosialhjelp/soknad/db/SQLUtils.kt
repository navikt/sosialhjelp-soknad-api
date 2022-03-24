package no.nav.sosialhjelp.soknad.db

import java.sql.Timestamp
import java.time.LocalDateTime

object SQLUtils {

    private const val HSQLDB = "hsqldb"
    const val DIALECT_PROPERTY = "sqldialect"

    fun limit(limit: Int): String {
        return if (HSQLDB == System.getProperty(DIALECT_PROPERTY)) {
            "limit $limit"
        } else {
            "and rownum <= $limit"
        }
    }

    fun selectNextSequenceValue(sequence: String): String {
        return if (HSQLDB == System.getProperty(DIALECT_PROPERTY)) {
            "call next value for $sequence"
        } else {
            "select $sequence.nextval from dual"
        }
    }

    fun tidTilTimestamp(tid: LocalDateTime?): Timestamp? {
        return if (tid != null) Timestamp.valueOf(tid) else null
    }

    fun nullableTimestampTilTid(timestamp: Timestamp?): LocalDateTime? {
        return timestamp?.toLocalDateTime()
    }

    fun timestampTilTid(timestamp: Timestamp): LocalDateTime {
        return timestamp.toLocalDateTime()
    }
}
