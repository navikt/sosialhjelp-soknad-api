@file:Suppress("unused")

package no.nav.sosialhjelp.soknad.app.config

import org.springframework.cache.annotation.Cacheable
import org.springframework.core.annotation.AliasFor
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
@Cacheable(cacheNames = ["cacheWithKey"])
annotation class CacheWithKey(
    @get:AliasFor(annotation = Cacheable::class, attribute = "cacheNames")
    val cacheNames: Array<String>,
    @get:AliasFor(annotation = Cacheable::class, attribute = "key")
    val key: String,
    @get:AliasFor(annotation = Cacheable::class, attribute = "unless")
    val unless: String = "",
)
