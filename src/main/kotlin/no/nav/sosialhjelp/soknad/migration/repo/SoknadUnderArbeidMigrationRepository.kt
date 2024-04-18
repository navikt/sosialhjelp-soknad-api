package no.nav.sosialhjelp.soknad.migration.repo

import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SoknadUnderArbeidMigrationRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val soknadUnderArbeidRowMapper = SoknadUnderArbeidRowMapper()

    fun getSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid? {
        return jdbcTemplate.query(
            "select * from soknad_under_arbeid where behandlingsid = ?",
            soknadUnderArbeidRowMapper,
            behandlingsId,
        ).firstOrNull()
    }

    fun count(): Int {
        return jdbcTemplate.queryForObject(
            "select count(*) from soknad_under_arbeid",
            Int::class.java,
        ) ?: 0
    }
}
