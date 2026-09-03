package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.util.UUID

object Utils {
    val sosialhjelpJsonMapper: JsonMapper =
        JsonSosialhjelpObjectMapper
            .createJsonMapperBuilder()
            .addModule(kotlinModule())
            .build()

    fun getDigisosIdFromResponse(
        errorMessage: String,
        soknadId: UUID,
    ): UUID? {
        listOf("finnes allerede", soknadId.toString(), "navEksternRefId", "DigisosId").forEach { if (!errorMessage.contains(it)) return null }

        return errorMessage
            .indexOf("DigisosId")
            .let { index -> errorMessage.substring(index) }
            .split(" ")
            .find { it.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) }
            ?.let { UUID.fromString(it) }
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
