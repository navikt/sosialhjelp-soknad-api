package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.sosialhjelpJsonMapper
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.util.UUID
import java.util.regex.Pattern

fun WebClient.Builder.configureCodecs(jsonMapper: JsonMapper = sosialhjelpJsonMapper): WebClient.Builder {
    codecs {
        it.defaultCodecs().maxInMemorySize(150 * 1024 * 1024)
        it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(jsonMapper))
        it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(jsonMapper))
    }

    return this
}

object Utils {
    val sosialhjelpJsonMapper: JsonMapper =
        JsonSosialhjelpObjectMapper
            .createJsonMapperBuilder()
            .addModule(kotlinModule())
            .build()

    fun getDigisosIdFromResponse(
        errorResponse: String,
        soknadId: UUID,
    ): UUID? {
        if (errorResponse.contains(soknadId.toString()) && errorResponse.contains("finnes allerede")) {
            val p = Pattern.compile("^.*?message.*([0-9a-fA-F]{8}[-]?(?:[0-9a-fA-F]{4}[-]?){3}[0-9a-fA-F]{12}).*?$")
            val m = p.matcher(errorResponse)
            if (m.matches()) {
                return m.group(1).let { UUID.fromString(it) }
            }
        }
        return null
    }

    fun stripVekkFnutter(tekstMedFnutt: String): String {
        return tekstMedFnutt.replace("\"", "")
    }

    fun createHttpEntity(
        body: Any,
        name: String,
        filename: String?,
        contentType: String,
    ): HttpEntity<Any> {
        val headerMap = LinkedMultiValueMap<String, String>()
        val builder: ContentDisposition.Builder =
            ContentDisposition
                .builder("form-data")
                .name(name)
        val contentDisposition: ContentDisposition =
            if (filename == null) builder.build() else builder.filename(filename).build()

        headerMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        headerMap.add(HttpHeaders.CONTENT_TYPE, contentType)
        return HttpEntity(body, HttpHeaders(headerMap))
    }
}
