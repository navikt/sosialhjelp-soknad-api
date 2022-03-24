package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.Companion.feilet
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.Companion.lyktes
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.PingMetadata
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Profile("!repositoryTest")
@Configuration
@EnableTransactionManagement
open class SoknadInnsendingDBConfig(
    private val dataSource: DataSource
) {

    @Bean
    open fun dbPing(): Pingable {
        return Pingable {
            val metadata = PingMetadata("jdbc/SoknadInnsendingDS", "JDBC:Sendsøknad Database", true)
            try {
                val jdbcTemplate = JdbcTemplate(dataSource)
                jdbcTemplate.queryForList("select * from dual")
                lyktes(metadata)
            } catch (e: Exception) {
                feilet(metadata, e)
            }
        }
    }
}
