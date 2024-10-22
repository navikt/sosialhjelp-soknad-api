package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe.ProductionComparatorManager
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ShadowProductionManager(
    private val jsonGenerator: JsonInternalSoknadGenerator,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createAndCompareShadowJson(
        soknadId: String,
        original: JsonInternalSoknad?,
    ) {
        original?.let {
            runCatching {
                val shadowJson = jsonGenerator.createJsonInternalSoknad(UUID.fromString(soknadId))

                JsonInternalSoknadListSorter(it, shadowJson).doSorting()

                // TODO Midlertidig utvidet logging av kjente feil i shadow prod
                ProductionComparatorManager(original = original, shadow = shadowJson)
                    .compareSpecificFields()

                // TODO Midlertidig nøyaktig logging av lister for sammenlikning
                if (MiljoUtils.isNonProduction()) {
                    JsonSoknadComparator(original = original, shadow = shadowJson).compareCollections()
                }

                // TODO Sammenlikner json-strukturen
                if (MiljoUtils.isNonProduction()) {
                    JsonContentComparator().doCompareAndLogErrors(it, shadowJson)
                }
            }
                .onFailure {
                    logger.warn("NyModell : Sammenlikning : Exception i sammenlikning av Json", it)
                }
        } ?: logger.error("NyModell : Sammenlikning : Original er null")
    }

    internal class JsonContentComparator {
        private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        private val logger = LoggerFactory.getLogger(this::class.java)

        fun <T : Any> doCompareAndLogErrors(
            original: T,
            other: T,
        ) {
            logger.info("*** COMPARING *** - baseClass: ${original::class.simpleName}")

            compare(mapper.writeValueAsString(original), mapper.writeValueAsString(other))
                .also {
                    JsonCompareErrorLogger(result = it).logAllErrors(asOneString = MiljoUtils.isProduction())
                }
        }

        private fun compare(
            original: String,
            other: String,
        ): JSONCompareResult = JSONCompare.compareJSON(original, other, JSONCompareMode.STRICT_ORDER)
    }
}
