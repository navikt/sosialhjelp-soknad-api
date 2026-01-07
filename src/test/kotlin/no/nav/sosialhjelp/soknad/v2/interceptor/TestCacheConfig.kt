package no.nav.sosialhjelp.soknad.v2.interceptor

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestCacheConfig {
    @Bean
    @Primary
    fun cacheManager(): CacheManager = NoOpCacheManager()
}
