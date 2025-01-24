package no.nav.sosialhjelp.soknad.v2.config

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Profile("prodgcp")
@Configuration
class FlywayConfig {
    @Bean
    fun flyway(dataSource: DataSource): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .load()
            .also { runSetup(it) }
    }

    fun runSetup(flyway: Flyway) {
        if (MiljoUtils.isProdFss()) return

        logger.info("Running clean migrate")
    }

    companion object {
        private val logger by logger()
    }
}
