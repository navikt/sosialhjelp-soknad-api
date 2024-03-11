package no.nav.sosialhjelp.soknad.v2.config

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Profile("!test & !mock-alt")
@Configuration
@EnableJdbcRepositories(basePackages = ["no.nav.sosialhjelp.soknad"])
class FlywayConfig(@Value("\${postgres.user.role}") private val role: String) {

    @Bean
    fun setRole(): FlywayConfigurationCustomizer {
        log.debug("Kjører set role for rolle $role")

        return FlywayConfigurationCustomizer { c: FluentConfiguration ->
            c.initSql("SET ROLE \"$role\"")
        }
    }

    companion object {
        private val log by logger()
    }
}
