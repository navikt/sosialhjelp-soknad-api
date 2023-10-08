package no.nav.sosialhjelp.soknad.repository.soknadunderarbeid

import no.nav.sosialhjelp.soknad.repository.SQLUtils
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.BatchOpplastetVedleggRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.sql.ResultSet
import java.time.LocalDateTime

/**
 * Repository for SoknadUnderArbeid.
 * Operasjoner som kun er tiltenkt batch/schedulerte jobber.
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
class BatchSoknadUnderArbeidRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
    private val batchOpplastetVedleggRepository: BatchOpplastetVedleggRepository
) : BatchSoknadUnderArbeidRepository {

    private val soknadUnderArbeidRowMapper = SoknadUnderArbeidRowMapper()

    override fun hentSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid? {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            behandlingsId
        ).firstOrNull()
    }

    override fun hentSoknadUnderArbeid(soknadUnderArbeidId: Long): SoknadUnderArbeid? {
        return jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where SOKNAD_UNDER_ARBEID_ID = ?",
            soknadUnderArbeidRowMapper,
            soknadUnderArbeidId
        ).firstOrNull()
    }

    override fun hentGamleSoknadUnderArbeidForBatch(): List<Long> {
        val datoMinusFjortenDager = LocalDateTime.now().minusDays(14)
        return jdbcTemplate.query(
            "select SOKNAD_UNDER_ARBEID_ID from SOKNAD_UNDER_ARBEID where SISTENDRETDATO < ? and STATUS = ?",
            { resultSet: ResultSet, _: Int -> resultSet.getLong("soknad_under_arbeid_id") },
            SQLUtils.tidTilTimestamp(datoMinusFjortenDager),
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
