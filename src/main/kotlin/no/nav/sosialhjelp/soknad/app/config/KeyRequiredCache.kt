@file:Suppress("unused")

package no.nav.sosialhjelp.soknad.app.config

import org.springframework.cache.annotation.Cacheable
import org.springframework.core.annotation.AliasFor
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Composed annotation wrapping Spring's [@Cacheable].
 *
 * Enforces that callers always supply:
 * - [cacheNames] — the name of the cache to use
 * - [key]        — a SpEL expression for the cache key (e.g. `"#myParam"`)
 *
 * Use [unless] to skip caching based on the result (e.g. `"#result == null"`).
 *
 * Example:
 * ```
 * @KeyRequiredCache(cacheNames = ["myCache"], key = "#id")
 * fun fetchById(id: String): MyData? { ... }
 * ```
 */
@Target(FUNCTION)
@MustBeDocumented
@Cacheable(cacheNames = [KeyRequiredCache.PLACEHOLDER])
annotation class KeyRequiredCache(
    // Overrides the placeholder value via @AliasFor at runtime
    @get:AliasFor(annotation = Cacheable::class, attribute = "cacheNames")
    val cacheNames: Array<String>,
    @get:AliasFor(annotation = Cacheable::class, attribute = "key")
    val key: String,
    @get:AliasFor(annotation = Cacheable::class, attribute = "unless")
    val unless: String = "",
) {
    companion object {
        /**
         * Placeholder value required by @Cacheable's non-nullable [cacheNames].
         * This value is never actually used at runtime — it is always overridden
         * by the [cacheNames] alias provided at the call site.
         */
        const val PLACEHOLDER = "__key_required_cache_placeholder__"
    }
}
