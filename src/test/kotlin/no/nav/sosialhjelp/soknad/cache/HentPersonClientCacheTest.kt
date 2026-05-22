package no.nav.sosialhjelp.soknad.cache

import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Profile

@Profile("test", "test-container", "no-redis")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class HentPersonClientCacheTest {
    @Autowired
    private lateinit var hentPersonClient: HentPersonClient

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Test
    fun `Hente person skal caches`() {
    }
}
