package no.nav.sosialhjelp.soknad.nymodell.repository

import no.nav.sosialhjelp.soknad.Application
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(classes = [Application::class])
@ActiveProfiles(profiles = ["no-redis", "test"])
abstract class WithTestcontainer {
    companion object Container : PostgreSQLContainer<Container>("postgres:16") {

        private val dbName = "postgres"
        private val dbUser = "postgres"
        private val dbPassword = "postgres"

        init {
            withDatabaseName(dbName)
            withUsername(dbUser)
            withPassword(dbPassword)
        }

        @BeforeAll
        @JvmStatic
        fun startContainer() {
            start()
            isRunning
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            stop()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerOracleProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                String.format("jdbc:postgresql://localhost:%d/postgres", firstMappedPort)
            }
            registry.add("spring.datasource.username") { dbUser }
            registry.add("spring.datasource.password") { dbPassword }
        }
    }
}
