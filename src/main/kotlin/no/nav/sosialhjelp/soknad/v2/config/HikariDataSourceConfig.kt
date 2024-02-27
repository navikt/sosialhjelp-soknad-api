package no.nav.sosialhjelp.soknad.v2.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test & !mock-alt")
@Configuration
class HikariDataSourceConfig(
    @Value("\${spring.datasource.url}") private val dbUrl: String,
    @Value("\${POSTGRES_USER_ROLE}") private val role: String,
    @Value("\${VAULT_MOUNT_PATH}") private val vaultMountPath: String
) {

    // TODO: sjekk ut evt mer config som minimumIdle, idleTimeout ++ hva må settes?

    @Bean
    fun dataSource(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = dbUrl

        return HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(config, vaultMountPath, role)
    }
}
