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
    fun jsonToHvoSokesOm(json: String): String? =
        runCatching { ObjectMapper().readValue(json, jacksonTypeRef<Kategorier>()) }
            .map {
                if (it.isNotEmpty()) {
                    // Nødhjelp skal komme øverst i søknaden
                    it.sortedByDescending { cat -> cat.text == "Nødhjelp" }.joinToString("\n", prefix = "Bruker har valgt følgende kategorier:\n") { kategori ->
                        when {
                            kategori.text == "Annet" -> "Annet:\n\t${kategori.hvaSokesOm ?: ""}"
                            kategori.subCategories.isNotEmpty() -> kategori.subCategories.joinToString("\n\t", prefix = "${kategori.text}:\n\t")
                            else -> kategori.text
                        }
                    }
                } else {
                    null
                }
            }.getOrNull()
}
