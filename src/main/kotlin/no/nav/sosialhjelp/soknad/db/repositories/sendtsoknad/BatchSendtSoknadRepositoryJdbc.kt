package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
open class BatchSendtSoknadRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
) : BatchSendtSoknadRepository {

    override fun hentSendtSoknadIdList(behandlingsIdList: List<String>): List<Long> {
//        val inSql = behandlingsIdList.joinToString(separator = ",") { "?" }
        return jdbcTemplate.query(
            "select * from SENDT_SOKNAD where BEHANDLINGSID IN (?)",
            { resultSet: ResultSet, _: Int -> resultSet.getLong("sendt_soknad_id") },
            behandlingsIdList.joinToString { it }
        )
    }

    override fun slettSendtSoknader(sendtSoknadIdList: List<Long>) {
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
//                val inSql = sendtSoknadIdList.joinToString(separator = ",") { "?" }
                jdbcTemplate.update(
                    "delete from SENDT_SOKNAD where SENDT_SOKNAD_ID IN (?)",
                    sendtSoknadIdList.joinToString { it.toString() }
                )
            }
        })
    }
}
