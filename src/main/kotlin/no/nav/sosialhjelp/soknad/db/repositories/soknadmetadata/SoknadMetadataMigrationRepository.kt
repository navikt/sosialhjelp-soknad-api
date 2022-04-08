package no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata

import no.nav.sosialhjelp.soknad.db.SQLUtils
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class SoknadMetadataMigrationRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getNextSoknadMetadataAfter(sistEndretTidspunkt: LocalDateTime): SoknadMetadata? {
        return jdbcTemplate.query(
            "select * from soknadmetadata where sistendretdato > ? order by sistendretdato asc",
            SoknadMetadataRowMapper.soknadMetadataRowMapper,
            SQLUtils.tidTilTimestamp(sistEndretTidspunkt)
        ).firstOrNull()
    }
}
