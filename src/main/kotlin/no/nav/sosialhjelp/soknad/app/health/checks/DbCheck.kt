package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DbCheck(
    private val jdbcTemplate: JdbcTemplate
) : DependencyCheck {

    override val type = DependencyType.DB
    override val name = "JDBC:Sendsøknad Database"
    override val address = "jdbc/SoknadInnsendingDS"
    override val importance = Importance.CRITICAL

    override fun doCheck() {
        jdbcTemplate.queryForList("select * from dual")
    }
}
