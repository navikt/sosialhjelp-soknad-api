package no.nav.sosialhjelp.soknad.v2.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
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
class DatabaseConfig(
    @Value("\${spring.datasource.url}") private val dbUrl: String,
    @Value("\${postgres.user.role}") private val role: String,
    @Value("\${vault-mount-path}") private val vaultMountPath: String
) {

    // TODO: sjekk ut evt mer config som minimumIdle, idleTimeout ++ hva må settes?

    @Bean
    fun dataSource(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = dbUrl

        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, vaultMountPath, role)
    }

    @Bean
    fun setRole(): FlywayConfigurationCustomizer {

        return FlywayConfigurationCustomizer { c: FluentConfiguration ->
            c.initSql("SET ROLE \"$role\"")
        }
    }
}
