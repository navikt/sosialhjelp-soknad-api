package no.nav.sosialhjelp.soknad.v2.config

import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test & !mock-alt")
@Configuration
// @EnableJdbcRepositories(basePackages = ["no.nav.sosialhjelp.soknad.v2"])
class FlywayConfig(@Value("\${postgres.user.role}") private val role: String) {

    @Bean
    fun setRole(): FlywayConfigurationCustomizer {
        return FlywayConfigurationCustomizer { c: FluentConfiguration ->
            c.initSql("SET ROLE \"$role\"")
        }
    }
}
