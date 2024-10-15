package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.compareStrings

class ForsorgerpliktAnsvarComparator(
    private val original: JsonForsorgerplikt,
    private val shadow: JsonForsorgerplikt,
) : ProductionComparator {
    override fun compare() {
        if (original == shadow) return

        original.ansvar.forEach { original ->
            shadow.ansvar
                .find { it.barn.personIdentifikator == original.barn.personIdentifikator }
                ?.let { ansvar ->
                    compareStrings(
                        ansvar.samvarsgrad.verdi?.toString(),
                        original.samvarsgrad.verdi?.toString(),
                        "samvarsgrad",
                        true,
                    )
                }
                ?.also { compareString ->
                    if (compareString.isNotEmpty() && compareString.isNotBlank()) {
                        logger.warn(
                            "NyModell: Felter i ForsorgerpliktAnsvar er ikke like: \n" +
                                "Comparison: $compareString\n\n",
                        )
                    }
                }
                ?: logger.warn("NyModell: ForsorgerpliktAnsvar ikke funnet i shadow: \n\n")
        }
    }

    companion object {
        private val logger by logger()
    }
}
