package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
open class BatchSendtSoknadRepositoryJdbc(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
) : BatchSendtSoknadRepository {

    override fun hentSendtSoknadIdList(behandlingsIdList: List<String>): List<Long> {
        val parameters = MapSqlParameterSource("ids", behandlingsIdList)
        return namedParameterJdbcTemplate.query(
            "select * from SENDT_SOKNAD where BEHANDLINGSID IN (:ids)",
            parameters
        ) { resultSet: ResultSet, _: Int -> resultSet.getLong("sendt_soknad_id") }
    }

    override fun slettSendtSoknader(sendtSoknadIdList: List<Long>) {
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                val parameters = MapSqlParameterSource("ids", sendtSoknadIdList)
                namedParameterJdbcTemplate.update(
                    "delete from SENDT_SOKNAD where SENDT_SOKNAD_ID IN (:ids)",
                    parameters
                )
            }
        })
    }
}
