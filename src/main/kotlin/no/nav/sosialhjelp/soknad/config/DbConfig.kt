package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.Clock
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
open class DbConfig(
    private val dataSource: DataSource
) {

    @Bean
    open fun clock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Bean
    open fun dbPing(): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata("jdbc/SoknadInnsendingDS", "JDBC:Sendsøknad Database", true)
            try {
                val jdbcTemplate = JdbcTemplate(dataSource)
                jdbcTemplate.queryForList("select * from dual")
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }
}
