package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.slf4j.LoggerFactory
import java.util.*

class JsonStructureComparator(private val soknadId: UUID) {

    private val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun <T: Any> doCompareAndLogErrors(original: T, other: T) {
        logger.info("$soknadId - *** COMPARING *** - baseClass: ${original::class.simpleName}")

        compare(mapper.writeValueAsString(original), mapper.writeValueAsString(other))
            .also {
                JsonCompareErrorLogger(soknadId, result = it).logAllErrors()
            }
    }

    fun compare(original: String, other: String): JSONCompareResult = JSONCompare
        .compareJSON(original, other, JSONCompareMode.STRICT_ORDER)
}
