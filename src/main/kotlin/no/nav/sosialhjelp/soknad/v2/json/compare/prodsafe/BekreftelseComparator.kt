package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.compareStrings
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.jsonMapper

class BekreftelseComparator(
    private val originals: List<JsonOkonomibekreftelse>?,
    private val shadows: List<JsonOkonomibekreftelse>?,
) : ProductionComparator {
    override fun compare() {
        compareSize()
        compareContent()
    }

    private fun compareSize() {
        if (originals?.size != shadows?.size) {
            logger.warn(
                "NyModell: Antall bekreftelser er ikke likt: \n\n" +
                    "Original: ${jsonMapper.writeValueAsString(originals)}\n\n" +
                    "Shadow: ${jsonMapper.writeValueAsString(shadows)}",
            )
        }
    }

    private fun compareContent() {
        if (originals === shadows) return

        originals?.forEach { original ->
            shadows?.find { it.type == original.type }
                ?.let { shadow ->
                    listOf(
                        compareStrings(original.kilde.name, shadow.kilde?.name, "kilde", true),
                        compareStrings(original.type, shadow.type, "type", true),
                        compareStrings(original.tittel, shadow.tittel, "tittel", true),
                        compareStrings(original.verdi?.toString(), shadow.verdi?.toString(), "verdi", true),
                        compareStrings(original.bekreftelsesDato, shadow.bekreftelsesDato, "bekreftetDato", true),
                    )
                        .filter { it != "" }
                        .joinToString("\n")
                }
                ?.also { compareString ->
                    if (compareString.isNotEmpty() && compareString.isNotBlank()) {
                        logger.warn(
                            "NyModell: Felter i Bekreftelse er ikke like: \n" +
                                "Comparison: $compareString\n\n",
                        )
                    }
                }
                ?: logger.warn(
                    "NyModell: Bekreftelse ikke funnet i shadow: \n\n" +
                        "Original: ${jsonMapper.writeValueAsString(original)}\n\n" +
                        "Shadow: ${jsonMapper.writeValueAsString(shadows)}",
                )
        }
    }

    companion object {
        private val logger by logger()
    }
}
