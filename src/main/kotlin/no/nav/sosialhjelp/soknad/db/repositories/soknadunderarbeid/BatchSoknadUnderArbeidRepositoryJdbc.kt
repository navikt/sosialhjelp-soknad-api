package no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid

import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.BatchOpplastetVedleggRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet
import java.util.Optional
import javax.inject.Inject
import javax.sql.DataSource

/**
 * Repository for SoknadUnderArbeid.
 * Operasjoner som kun er tiltenkt batch/schedulerte jobber.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Component
class BatchSoknadUnderArbeidRepositoryJdbc(
    private val transactionTemplate: TransactionTemplate,
    private val batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository
) : NamedParameterJdbcDaoSupport(), BatchSoknadUnderArbeidRepository {

    private val soknadUnderArbeidRowMapper = SoknadUnderArbeidRowMapper()

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    override fun hentSoknadUnderArbeidIdFromBehandlingsIdOptional(behandlingsId: String?): Optional<Long> {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
            { resultSet: ResultSet, _: Int -> resultSet.getLong("soknad_under_arbeid_id") },
            behandlingsId
        ).stream().findFirst()
    }

    override fun hentGamleSoknadUnderArbeidForBatch(): List<Long> {
        return jdbcTemplate.query(
            "select SOKNAD_UNDER_ARBEID_ID from SOKNAD_UNDER_ARBEID where SISTENDRETDATO < CURRENT_TIMESTAMP - (INTERVAL '14' DAY) and STATUS = ?",
            { resultSet: ResultSet, _: Int -> resultSet.getLong("soknad_under_arbeid_id") },
            SoknadUnderArbeidStatus.UNDER_ARBEID.toString()
        )
    }

    override fun slettSoknad(soknadUnderArbeidId: Long?) {
        transactionTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(transactionStatus: TransactionStatus) {
                if (soknadUnderArbeidId == null) {
                    throw RuntimeException("Kan ikke slette sendt søknad uten søknadsid")
                }
                batchOpplastetVedleggRepository.slettAlleVedleggForSoknad(soknadUnderArbeidId)
                jdbcTemplate.update(
                    "delete from SOKNAD_UNDER_ARBEID where SOKNAD_UNDER_ARBEID_ID = ?",
                    soknadUnderArbeidId
                )
            }
        })
    }

    override fun hentForeldedeEttersendelser(): List<SoknadUnderArbeid> {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where SISTENDRETDATO < CURRENT_TIMESTAMP - (INTERVAL '1' HOUR) and TILKNYTTETBEHANDLINGSID IS NOT NULL and STATUS = ?",
            soknadUnderArbeidRowMapper,
            SoknadUnderArbeidStatus.UNDER_ARBEID.toString()
        )
    }
}
