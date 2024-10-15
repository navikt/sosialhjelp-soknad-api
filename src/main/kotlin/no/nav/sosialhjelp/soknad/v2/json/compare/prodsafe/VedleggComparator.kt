package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger

class VedleggComparator(
    private val originals: List<JsonVedlegg>,
    private val shadows: List<JsonVedlegg>,
) : ProductionComparator {
    override fun compare() {
        if (originals == shadows) return

        val originalTypes =
            originals.map {
                "${it.type}|${it.tilleggsinfo} - ${it.status} - filer: ${it.filer.size}"
            }
        val shadowTypes =
            shadows.map {
                "${it.type}|${it.tilleggsinfo} - ${it.status} - filer: ${it.filer.size}"
            }

        logger.warn(
            "NyModell: Vedlegg er ikke like: \n\n" +
                "Original: $originalTypes\n\n" +
                "Shadow: $shadowTypes",
        )
    }

    companion object {
        private val logger by logger()
    }
}
