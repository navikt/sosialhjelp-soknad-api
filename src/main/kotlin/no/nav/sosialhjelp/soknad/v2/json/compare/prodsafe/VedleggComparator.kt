package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager.Companion.jsonMapper

class VedleggComparator(
    private val originals: List<JsonVedlegg>,
    private val shadows: List<JsonVedlegg>,
) : ProductionComparator {
    override fun compare() {
        if (relevantFieldsAreTheSame()) return

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
                "Original: ${jsonMapper.writeValueAsString(originalTypes)}\n\n" +
                "Shadow: ${jsonMapper.writeValueAsString(shadowTypes)}",
        )
    }

    private fun relevantFieldsAreTheSame(): Boolean {
        if (originals.size != shadows.size) {
            logger.warn("NyModell: Antall vedlegg er forskjellig")
            return false
        }

        originals.forEach { original ->
            if (!isVedleggExistsAndValid(original)) {
                logger.warn("NyModell: Vedlegg er ikke valid")
                return false
            }
        }
        return true
    }

    private fun isVedleggExistsAndValid(original: JsonVedlegg): Boolean {
        shadows.find { it.type == original.type }
            ?.also { shadowVedlegg ->
                if (shadowVedlegg.type != original.type) {
                    logger.warn("NyModell: Vedlegg.Type er ikke lik: ${original.type} - ${shadowVedlegg.type}")
                    return false
                }
                if (shadowVedlegg.tilleggsinfo != original.tilleggsinfo) {
                    logger.warn("NyModell: Vedlegg.Tilleggsinfo er ikke lik: ${original.tilleggsinfo} - ${shadowVedlegg.tilleggsinfo}")
                    return false
                }
                if (shadowVedlegg.filer.size != original.filer.size) {
                    logger.warn("NyModell: Vedlegg.Filer.size er ikke lik: ${original.filer.size} - ${shadowVedlegg.filer.size}")
                    return false
                }
                if (shadowVedlegg.status != original.status) {
                    logger.warn("NyModell: Vedlegg.Status er ikke lik: ${original.status} - ${shadowVedlegg.status}")
                    return false
                }
            }
            ?: return false
        return true
    }

    companion object {
        private val logger by logger()
    }
}
