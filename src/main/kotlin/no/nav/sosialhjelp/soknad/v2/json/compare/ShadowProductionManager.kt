package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.v2.json.generate.JsonInternalSoknadGenerator
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class ShadowProductionManager(
    private val jsonGenerator: JsonInternalSoknadGenerator
) {
    fun compareShadowProduction(soknadUnderArbeid: SoknadUnderArbeid) {
        val logger = LoggerFactory.getLogger(this::class.java)

        kotlin.runCatching {

            soknadUnderArbeid.jsonInternalSoknad?.let { original ->
                val soknadId = UUID.fromString(soknadUnderArbeid.behandlingsId)
                val shadowJson = jsonGenerator.createJsonInternalSoknad(soknadId)

                JsonContentComparator(soknadId).doCompareAndLogErrors(original, shadowJson)
            }
                ?: logger.error("NyModell: Sammenlikning - Orginal Json er tom.")
        }
            .onFailure {
                logger.error("NyModell: Sammenlikning - Exception i sammenlikning av Json", it)
            }
    }

    class JsonContentComparator(
        private val soknadId: UUID
    ) {
        private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        private val logger = LoggerFactory.getLogger(this::class.java)

        fun <T : Any> doCompareAndLogErrors(original: T, other: T) {
            logger.info("$soknadId - *** COMPARING *** - baseClass: ${original::class.simpleName}")

            compare(mapper.writeValueAsString(original), mapper.writeValueAsString(other))
                .also {
                    JsonCompareErrorLogger(soknadId, result = it).logAllErrors()
                }
        }
        private fun compare(original: String, other: String): JSONCompareResult = JSONCompare
            .compareJSON(original, other, JSONCompareMode.STRICT_ORDER)
    }
}
