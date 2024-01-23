package no.nav.sosialhjelp.soknad.db.repositories

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@Testcontainers
@ActiveProfiles("test", "no-redis")
class RepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", PostgresContainerObject::getJdbcUrl)
            registry.add("spring.datasource.username", PostgresContainerObject::getUsername)
            registry.add("spring.datasource.password", PostgresContainerObject::getPassword)
        }
//        @JvmStatic
//        @BeforeAll
//        fun startContainer() {
//            if (!PostgresContainerObject.isRunning) {
//                PostgresContainerObject.start()
//            }
//        }
//
//        @JvmStatic
//        @AfterAll
//        fun stopContainer() {
//            if (PostgresContainerObject.isRunning) {
//                PostgresContainerObject.stop()
//            }
//        }
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
