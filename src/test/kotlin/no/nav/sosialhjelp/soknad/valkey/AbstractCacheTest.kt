package no.nav.sosialhjelp.soknad.valkey

import com.ninjasquad.springmockk.SpykBean
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractCacheTest(private val cacheName: String) {
    @SpykBean
    protected lateinit var cacheManager: CacheManager

    protected val cache get() = cacheManager.getCache(cacheName) ?: error("Cache not found: $cacheName")

    @BeforeEach
    fun setup() {
        cache.clear()
        clearAllMocks()
    }

    abstract fun `Verdi skal lagres i cache`()

    abstract fun `Skal hente fra client hvis cache er utilgjengelig eller feiler`()

    abstract fun `Skal ikke hente fra client hvis verdi finnes i cache`()

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
