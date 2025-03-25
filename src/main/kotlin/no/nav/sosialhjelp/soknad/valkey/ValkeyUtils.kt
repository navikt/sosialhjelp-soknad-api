package no.nav.sosialhjelp.soknad.valkey

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import org.slf4j.LoggerFactory
import java.io.IOException

object ValkeyUtils {
    private val log = LoggerFactory.getLogger(ValkeyUtils::class.java)

    val valkeyObjectMapper: ObjectMapper =
        JsonSosialhjelpObjectMapper.createObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

    fun toKommuneInfoMap(value: ByteArray?): Map<String, KommuneInfo>? {
        if (value != null) {
            try {
                return valkeyObjectMapper.readValue<Array<KommuneInfo>>(value)
                    .associateBy { it.kommunenummer }
            } catch (e: IOException) {
                log.warn("noe feilet ved deserialisering til kommuneInfoMap", e)
            }
        }
        return null
    }
}
