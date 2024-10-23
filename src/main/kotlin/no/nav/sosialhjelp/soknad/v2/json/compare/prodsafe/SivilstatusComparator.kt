package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
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
            listOf(
                compareStrings(original?.kilde?.name, shadow?.kilde?.name, "kilde", true),
                compareStrings(original?.status?.name, shadow?.status?.name, "status"),
                compareStrings(
                    original?.folkeregistrertMedEktefelle?.toString(),
                    shadow?.folkeregistrertMedEktefelle?.toString(),
                    "folkeregistrertMedEktefelle",
                ),
                compareStrings(
                    original?.borSammenMed?.toString(),
                    shadow?.borSammenMed?.toString(),
                    "borSammenMed",
                ),
                compareEktefelle(),
            )
                .filter { it != "" }
                .joinToString("\n")

        if (compareString.isNotEmpty() && compareString.isNotBlank()) {
            logger.warn(
                "NyModell: Felter i Sivilstatus er ikke like: \n" +
                    "Comparison: $compareString\n\n",
            )
        }
    }

    private fun checkOriginalEmpty() {
        val isEmpty =
            original?.folkeregistrertMedEktefelle?.toString().isNullOrBlank() &&
                original?.borSammenMed?.toString().isNullOrBlank() && original?.ektefelle?.personIdentifikator.isNullOrBlank()
        if (isEmpty) return

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
        val orgEktefelle = original?.ektefelle
        val shadowEktefelle = shadow?.ektefelle

        // TODO Teit midlertidig if fordi mock-alt-api randomizer fødselsdato
        return listOf(
            compareStrings(orgEktefelle?.personIdentifikator, shadowEktefelle?.personIdentifikator, "personIdentifikator"),
            compareStrings(
                value1 = if (MiljoUtils.isProduction()) orgEktefelle?.fodselsdato else "",
                value2 = if (MiljoUtils.isProduction()) shadowEktefelle?.fodselsdato else "",
                fieldName = "fodselsdato",
            ),
            compareStrings(
                value1 = orgEktefelle?.navn?.etternavn,
                value2 = shadowEktefelle?.navn?.etternavn,
                fieldName = "kilde",
                typeValues = true,
            ),
        )
            .filter { it != "" }
            .joinToString("\n")
    }

    companion object {
        private val logger by logger()
    }
}
