package no.nav.sosialhjelp.soknad.v2.config

import no.nav.sosialhjelp.soknad.app.MiljoUtils
import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class FlywayConfig {
    @Profile("!(prodgcp|preprod)")
    @Bean
    fun dev(): FlywayMigrationStrategy {
        if (MiljoUtils.isProduction()) {
            throw IllegalStateException("Flyway skal ikke kjøres i produksjon")
        }

        return FlywayMigrationStrategy { flyway ->
            flyway.clean()
            flyway.migrate()
        }
    }

    @Profile("preprod")
    @Bean
    fun preprod(): FlywayMigrationStrategy {
        if (MiljoUtils.isProduction()) {
            throw IllegalStateException("Flyway skal ikke kjøres i produksjon")
        }

        return FlywayMigrationStrategy { flyway ->
            if (flyway.cleanOnStartup()) flyway.clean()
            flyway.migrate()
        }
    }
}

private fun Flyway.cleanOnStartup(): Boolean = !configuration.isCleanDisabled
