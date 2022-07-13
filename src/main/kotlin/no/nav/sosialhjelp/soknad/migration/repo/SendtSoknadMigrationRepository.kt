package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknadRowMapper.sendtSoknadRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class SendtSoknadMigrationRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getSendtSoknad(behandlingsId: String): SendtSoknad? {
        return jdbcTemplate.query(
            "select * from sendt_soknad where behandlingsid = ?",
            sendtSoknadRowMapper,
            behandlingsId
        ).firstOrNull()
    }

    fun count(): Int {
        return jdbcTemplate.queryForObject(
            "select count(*) from sendt_soknad",
            Int::class.java
        ) ?: 0
    }
}
