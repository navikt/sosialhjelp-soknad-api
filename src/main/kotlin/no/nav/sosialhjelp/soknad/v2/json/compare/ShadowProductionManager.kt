package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

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
            kotlin.runCatching {
                jsonGenerator.copyAndMerge(soknadId, it).let { copy ->
                    // sortere ansvar før sammenlikning
                    sortAnsvar(it, copy)
                    JsonContentComparator().doCompareAndLogErrors(it, copy)
                }
            }
                .onFailure {
                    logger.warn("NyModell : Sammenlikning : Exception i sammenlikning av Json", it)
                }
        } ?: logger.warn("NyModell : Sammenlikning : Original er null")
    }

    private fun sortAnsvar(original: JsonInternalSoknad, copy: JsonInternalSoknad) {
        original.soknad.data.familie.forsorgerplikt.ansvar.sortBy { it.barn.fodselsdato }
        copy.soknad.data.familie.forsorgerplikt.ansvar.sortBy { it.barn.fodselsdato }
    }

    internal class JsonContentComparator() {

        private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        private val logger = LoggerFactory.getLogger(this::class.java)

        fun <T : Any> doCompareAndLogErrors(
            original: T,
            other: T,
        ) {
            logger.info("*** COMPARING *** - baseClass: ${original::class.simpleName}")

            compare(mapper.writeValueAsString(original), mapper.writeValueAsString(other))
                .also {
                    JsonCompareErrorLogger(result = it,).logAllErrors(asOneString = true)
                }
        }

        private fun compare(
            original: String,
            other: String,
        ): JSONCompareResult =
            JSONCompare
                .compareJSON(original, other, JSONCompareMode.STRICT_ORDER)
    }
}
