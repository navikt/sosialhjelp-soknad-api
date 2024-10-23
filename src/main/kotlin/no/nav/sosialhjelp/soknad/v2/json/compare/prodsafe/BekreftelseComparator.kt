package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.compareStrings
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.jsonMapper
import java.time.ZonedDateTime

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
                "*** NyModell: Antall bekreftelser er ikke like *** " +
                    "ORG -> ${jsonMapper.writeValueAsString(originals)}, " +
                    "SHADOW -> ${jsonMapper.writeValueAsString(shadows)}",
            )
        }
    }

    private fun compareContent() {
        if (originals == shadows) return

        originals?.forEach { original ->

            shadows?.find { it.type == original.type }
                ?.let { shadow ->
                    listOf(
                        "{ ORG type -> ${original.type},  SHADOW type -> ${shadow.type} }, ",
                        compareStrings(original.kilde.name, shadow.kilde?.name, "kilde", true),
                        compareStrings(original.type, shadow.type, "type", true),
                        compareStrings(original.tittel, shadow.tittel, "tittel", true),
                        compareStrings(original.verdi?.toString(), shadow.verdi?.toString(), "verdi", true),
                        compareBekreftelsesDato(original.bekreftelsesDato, shadow.bekreftelsesDato),
                    )
                        .filter { it != "" }
                        .let { if (it.size > 1) it else emptyList() }
                }
                ?.also { compareList ->
                    val compareString = compareList.joinToString(separator = "; ")
                    if (compareString.isNotEmpty() && compareString.isNotBlank()) {
                        logger.warn(
                            "*** NyModell: Felter i Bekreftelse er ikke like *** " +
                                "COMPARISON : $compareString ",
                        )
                    }
                }
                ?: logger.warn(
                    "NyModell: Bekreftelse ikke funnet i shadow : " +
                        "ORG -> ${jsonMapper.writeValueAsString(original)}, " +
                        "SHADOW -> ${jsonMapper.writeValueAsString(shadows)} ",
                )
        }
    }

    private fun compareBekreftelsesDato(
        originalTimestamp: String?,
        shadowTimestamp: String?,
    ): String {
        if (originalTimestamp == null || shadowTimestamp == null) {
            return "En eller begge Bekreftelsesdato er null : " +
                "{ ORG -> $originalTimestamp, " +
                "SHADOW -> $shadowTimestamp } "
        }
        val original = ZonedDateTime.parse(originalTimestamp).toLocalDateTime()
        val shadow = ZonedDateTime.parse(shadowTimestamp).toLocalDateTime()

        if (shadow.isBefore(original.plusSeconds(5)) && shadow.isAfter(original.minusSeconds(5))) {
            return ""
        }
        return "Bekreftelsedata er ikke lik : " +
            "{ ORG -> $originalTimestamp, " +
            "SHADOW -> $shadowTimestamp } "
    }

    companion object {
        private val logger by logger()
    }
}
