package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.compareStrings

class BeskrivelseAvAnnetComparator(
    private val original: JsonOkonomibeskrivelserAvAnnet?,
    private val shadow: JsonOkonomibeskrivelserAvAnnet?,
) : ProductionComparator {
    override fun compare() {
        if (original == shadow) return
        if (shadow == null && original != null && original.areAllFieldsNullOrEmpty()) {
            return
        }
        if (original == null && shadow != null && shadow.areAllFieldsNullOrEmpty()) {
            return
        }

        val compareString =
            listOf(
                compareStrings(original?.kilde?.name, shadow?.kilde?.name, "kilde", true),
                compareStrings(original?.verdi, shadow?.verdi, "verdi"),
                compareStrings(original?.sparing, shadow?.sparing, "sparing"),
                compareStrings(original?.utbetaling, shadow?.utbetaling, "utbetaling"),
                compareStrings(original?.boutgifter, shadow?.boutgifter, "boutgifter"),
                compareStrings(original?.barneutgifter, shadow?.barneutgifter, "barneutgifter"),
            )
                .filter { it != "" }
                .joinToString("\n")

        logger.warn(
            "NyModell: Felter i BeskrivelseAvAnnet er ikke like: \n" +
                "Comparison: $compareString\n\n",
        )
    }

    private fun JsonOkonomibeskrivelserAvAnnet.areAllFieldsNullOrEmpty(): Boolean {
        return listOf(verdi, sparing, boutgifter, utbetaling, barneutgifter).all { field -> field.isNullOrEmpty() }
    }

    companion object {
        private val logger by logger()
    }
}
