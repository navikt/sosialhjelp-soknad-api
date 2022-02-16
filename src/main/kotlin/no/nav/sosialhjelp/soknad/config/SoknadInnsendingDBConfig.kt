package no.nav.sosialhjelp.soknad.config

import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.Companion.feilet
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.Companion.lyktes
import no.nav.sosialhjelp.soknad.health.selftest.Pingable.PingMetadata
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import javax.annotation.Resource
import javax.sql.DataSource

@Profile("!(mock-alt | test)")
@Configuration
@Resource(name = "jdbc/SoknadInnsendingDS", type = DataSource::class, lookup = "jdbc/SoknadInnsendingDS")
@EnableTransactionManagement
open class SoknadInnsendingDBConfig {

    @Bean
    open fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    open fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate {
        return TransactionTemplate(transactionManager)
    }

    @Bean
    open fun dbPing(dataSource: DataSource): Pingable {
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
