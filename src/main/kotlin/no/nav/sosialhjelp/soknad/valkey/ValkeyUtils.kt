package no.nav.sosialhjelp.soknad.valkey

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper

object ValkeyUtils {
    val valkeyObjectMapper: ObjectMapper =
        JsonSosialhjelpObjectMapper.createObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
}
