package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.db.SQLUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
open class SoknadMetadataMigrationRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getNextSoknadMetadataAfter(sistEndretDato: LocalDateTime): SoknadMetadata? {
        return jdbcTemplate.query(
            "select * from soknadmetadata where sistendretdato > ? order by sistendretdato asc",
            SoknadMetadataRowMapper.soknadMetadataRowMapper,
            SQLUtils.tidTilTimestamp(sistEndretDato)
        ).firstOrNull()
    }
}
