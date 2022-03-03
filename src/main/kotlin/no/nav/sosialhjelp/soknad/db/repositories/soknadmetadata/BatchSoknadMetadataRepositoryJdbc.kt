package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRowMapper.soknadMetadataRowMapper
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional
import javax.inject.Inject
import javax.sql.DataSource

@Component
open class BatchSoknadMetadataRepositoryJdbc : NamedParameterJdbcDaoSupport(), BatchSoknadMetadataRepository {

    @Inject
    fun setDS(ds: DataSource) {
        super.setDataSource(ds)
    }

    @Transactional
    override fun hentForBatch(antallDagerGammel: Int): Optional<SoknadMetadata> {
        val frist = LocalDateTime.now().minusDays(antallDagerGammel.toLong())
        while (true) {
            val resultat = jdbcTemplate.query(
                "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG' AND innsendingstatus = 'UNDER_ARBEID' " + SQLUtils.limit(1),
                soknadMetadataRowMapper,
                SQLUtils.tidTilTimestamp(frist)
            ).stream().findFirst()

            if (!resultat.isPresent) {
                return Optional.empty()
            }

            val rowsAffected = jdbcTemplate.update(
                "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'",
                resultat.get().id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    @Transactional
    override fun hentEldreEnn(antallDagerGammel: Int): Optional<SoknadMetadata> {
        val frist = LocalDateTime.now().minusDays(antallDagerGammel.toLong())
        while (true) {
            val resultat = jdbcTemplate.query(
                "SELECT * FROM soknadmetadata WHERE opprettetDato < ? AND batchstatus = 'LEDIG'" + SQLUtils.limit(1),
                soknadMetadataRowMapper,
                SQLUtils.tidTilTimestamp(frist)
            ).stream().findFirst()

            if (!resultat.isPresent) {
                return Optional.empty()
            }

            val rowsAffected = jdbcTemplate.update(
                "UPDATE soknadmetadata set batchstatus = 'TATT' WHERE id = ? AND batchstatus = 'LEDIG'",
                resultat.get().id
            )
            if (rowsAffected == 1) {
                return resultat
            }
        }
    }

    @Transactional
    override fun leggTilbakeBatch(id: Long) {
        jdbcTemplate.update("UPDATE soknadmetadata set batchstatus = 'LEDIG' WHERE id = ?", id)
    }

    @Transactional
    override fun slettSoknadMetaData(behandlingsId: String) {
        jdbcTemplate.update("DELETE FROM soknadmetadata WHERE behandlingsid = ?", behandlingsId)
    }
}
