package no.nav.sosialhjelp.soknad.app.config

import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class CacheMetricsConfig(
    val cacheMetricsRegistrar: CacheMetricsRegistrar,
    val cacheManager: CacheManager
) {
    @EventListener(ApplicationStartedEvent::class)
    fun addCachesToMetrics() = cacheMetricsRegistrar.bindCacheToRegistry(cacheManager.getCache("kodeverk"))
}
