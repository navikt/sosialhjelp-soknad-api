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

    override fun hentSendtSoknad(behandlingsId: String): Long? {
        return jdbcTemplate.query(
            "select * from SENDT_SOKNAD where BEHANDLINGSID = ?",
            { resultSet: ResultSet, _: Int -> resultSet.getLong("sendt_soknad_id") },
            behandlingsId
        ).firstOrNull()
    }

    override fun slettSendtSoknad(sendtSoknadId: Long) {
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                jdbcTemplate.update("delete from SENDT_SOKNAD where SENDT_SOKNAD_ID = ?", sendtSoknadId)
            }
        })
    }
}
