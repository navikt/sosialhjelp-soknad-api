package no.nav.sosialhjelp.soknad.valkey

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractCacheTest {
    @Autowired
    protected lateinit var cacheManager: CacheManager

    protected companion object ValkeyContainer : GenericContainer<ValkeyContainer>("valkey/valkey:7.2.8-alpine") {
        init {
            addFixedExposedPort(6379, 6379)
            withReuse(true)
        }

        @BeforeAll
        @JvmStatic
        fun startContainer() {
            start()
        }

        @AfterAll
        @JvmStatic
        fun stopContainer() {
            stop()
        }
    }
}
