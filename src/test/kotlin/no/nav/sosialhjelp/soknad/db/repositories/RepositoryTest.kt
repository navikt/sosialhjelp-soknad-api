package no.nav.sosialhjelp.soknad.db.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test", "no-redis")
class RepositoryTest {

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", PostgresContainerObject::getJdbcUrl)
            registry.add("spring.datasource.username", PostgresContainerObject::getUsername)
            registry.add("spring.datasource.password", PostgresContainerObject::getPassword)
        }
    }
}

object PostgresContainerObject: PostgreSQLContainer<PostgresContainerObject>(
    "postgres:15-alpine"
) {
    init {
        if(!isRunning) {
            start()
        }
    }
}
