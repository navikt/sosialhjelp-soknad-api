package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.compareStrings

class SivilstatusComparator(
    val original: JsonSivilstatus?,
    val shadow: JsonSivilstatus?,
) : ProductionComparator {
    override fun compare() {
        if (original == shadow) return
        if (original != null && shadow == null) {
            checkOriginalEmpty()
            return
        }

        val compareString =
            "${compareStrings(original?.kilde?.name, shadow?.kilde?.name, "kilde", true)}" +
                "${compareStrings(original?.status?.name, shadow?.status?.name, "status")}" +
                "${
                    compareStrings(
                        original?.folkeregistrertMedEktefelle?.toString(),
                        shadow?.folkeregistrertMedEktefelle?.toString(),
                        "folkeregistrertMedEktefelle",
                    )
                }" +
                "${
                    compareStrings(
                        original?.borSammenMed?.toString(),
                        shadow?.borSammenMed?.toString(),
                        "borSammenMed",
                    )
                }" +
                "${compareEktefelle()}"

        if (compareString.isNotEmpty() && compareString.isNotBlank()) {
            logger.warn(
                "NyModell: Felter i Sivilstatus er ikke like: \n" +
                    "Comparison: $compareString\n\n",
            )
        }
    }

    private fun checkOriginalEmpty() {
        val compareString =
            listOf(
                isNullOrBlank(original?.kilde?.name, "kilde"),
                isNullOrBlank(original?.status?.name, "status"),
                isNullOrBlank(original?.folkeregistrertMedEktefelle?.toString(), "folkeregistrertMedEktefelle"),
                isNullOrBlank(original?.borSammenMed?.toString(), "borSammenMed"),
                isNullOrBlank(original?.ektefelle?.personIdentifikator, "ektefelle"),
            )
                .filter { it != "" }
                .joinToString("\n")

        logger.warn(
            "NyModell: Shadow er null, original er: \n\n" +
                "Original: $compareString\n\n",
        )
    }

    private fun isNullOrBlank(
        value: String?,
        fieldName: String,
    ): String {
        return if (value.isNullOrBlank()) {
            "$fieldName er null eller tom"
        } else {
            ""
        }
    }

    private fun compareEktefelle(): String {
        return if (original?.ektefelle != shadow?.ektefelle) {
            "Ektefelle er ikke likt"
        } else {
            ""
        }
    }

    companion object {
        private val logger by logger()
    }
}
