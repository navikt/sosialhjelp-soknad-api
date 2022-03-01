package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet
import java.util.Optional
import javax.inject.Inject
import javax.inject.Named
import javax.sql.DataSource

@Named("BatchSendtSoknadRepository")
@Component
class BatchSendtSoknadRepositoryJdbc : NamedParameterJdbcDaoSupport(), BatchSendtSoknadRepository {

    @Inject
    private val transactionTemplate: TransactionTemplate? = null

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun hentSendtSoknad(behandlingsId: String): Optional<Long> {
        return jdbcTemplate.query(
            "select * from SENDT_SOKNAD where BEHANDLINGSID = ?",
            { resultSet: ResultSet, i: Int -> resultSet.getLong("sendt_soknad_id") },
            behandlingsId
        ).stream().findFirst()
    }

    override fun slettSendtSoknad(sendtSoknadId: Long) {
        transactionTemplate!!.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                if (sendtSoknadId == null) {
                    throw RuntimeException("Kan ikke slette sendt søknad uten søknadsid")
                }
                jdbcTemplate.update("delete from SENDT_SOKNAD where SENDT_SOKNAD_ID = ?", sendtSoknadId)
            }
        })
    }
}
