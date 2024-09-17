package no.nav.sosialhjelp.soknad.begrunnelse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

private data class Kategori(
    val text: String = "",
    val hvaSokesOm: String? = null,
    val subCategories: List<String> = emptyList(),
)

private typealias Kategorier = List<Kategori>

object BegrunnelseUtils {
    fun jsonToHvaSokesOm(json: String): String? {
        val kategorier = runCatching { ObjectMapper().readValue(json, jacksonTypeRef<Kategorier>()) }.getOrNull()?.takeIf { it.isNotEmpty() } ?: return null
        val nodhjelp = kategorier.find { it.text == "Nødhjelp" }?.takeIf { it.subCategories.isNotEmpty() }
        val annet = kategorier.find { it.text == "Annet" }?.takeIf { it.hvaSokesOm?.isNotBlank() == true }
        val resten = kategorier.filter { it.text != "Annet" && it.text != "Nødhjelp" }.takeIf { it.isNotEmpty() }

        return """
            |${nodhjelp?.let { "Nødhjelp: ${nodhjelp.subCategories.joinToString(", ").lowercase()}" } ?: ""}
            |${resten?.let { "Jeg søker om penger til:\n${resten.joinToString(", ") { it.text }}}" } ?: ""}
            |${annet?.let { "Annet:\n${it.hvaSokesOm ?: ""}" } ?: ""}
            """.trimMargin()
    }

    fun isEmptyJson(json: String): Boolean = runCatching { ObjectMapper().readValue(json, jacksonTypeRef<Kategorier>()) }.getOrNull()?.isEmpty() ?: false
}
