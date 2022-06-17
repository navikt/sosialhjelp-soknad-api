package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRowMapper.soknadMetadataRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Repository
open class BatchSoknadMetadataRepositoryJdbc(
    private val jdbcTemplate: JdbcTemplate,
) : BatchSoknadMetadataRepository {

    @Transactional
    override fun hentForBatch(antallDagerGammel: Int): SoknadMetadata? {
        val frist = LocalDateTime.now().minusDays(antallDagerGammel.toLong())
        while (true) {
            val resultat = jdbcTemplate.query(
                "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG' AND innsendingstatus = 'UNDER_ARBEID' " + SQLUtils.limit(1),
                soknadMetadataRowMapper,
                SQLUtils.tidTilTimestamp(frist)
            ).firstOrNull() ?: return null

            val rowsAffected = jdbcTemplate.update(
                "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'",
                resultat.id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    @Transactional
    override fun hentEldreEnn(antallDagerGammel: Int): List<SoknadMetadata> {
        val frist = LocalDateTime.now().minusDays(antallDagerGammel.toLong())
        while (true) {
            val resultat = jdbcTemplate.query(
                "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG'" + SQLUtils.limit(20),
                soknadMetadataRowMapper,
                SQLUtils.tidTilTimestamp(frist)
            )

            val rowsAffected = resultat.sumOf {
                jdbcTemplate.update(
                    "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'",
                    it.id
                )
            }

            if (rowsAffected == resultat.size) {
                return resultat
            }
        }
    }

    @Transactional
    override fun leggTilbakeBatch(id: Long) {
        jdbcTemplate.update("UPDATE soknadmetadata set batchstatus = 'LEDIG' WHERE id = ?", id)
    }

    @Transactional
    override fun slettSoknadMetaDataer(behandlingsIdList: List<String>) {
//        val inSql = behandlingsIdList.joinToString(separator = ",") { "?" }
        jdbcTemplate.update(
            "DELETE FROM soknadmetadata WHERE behandlingsid IN (?)",
            behandlingsIdList.joinToString { it }
        )
    }
}
