package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import java.util.regex.Pattern

object Utils {
    val digisosObjectMapper = JsonSosialhjelpObjectMapper
        .createObjectMapper()
        .configure(ORDER_MAP_ENTRIES_BY_KEYS, true)
        .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
        .registerKotlinModule()

    fun getDigisosIdFromResponse(errorResponse: String, behandlingsId: String): String? {
        if (errorResponse.contains(behandlingsId) && errorResponse.contains("finnes allerede")) {
            val p = Pattern.compile("^.*?message.*([0-9a-fA-F]{8}[-]?(?:[0-9a-fA-F]{4}[-]?){3}[0-9a-fA-F]{12}).*?$")
            val m = p.matcher(errorResponse)
            if (m.matches()) {
                return m.group(1)
            }
        }
        return null
    }

    fun stripVekkFnutter(tekstMedFnutt: String): String {
        return tekstMedFnutt.replace("\"", "")
    }

    fun createHttpEntity(body: Any, name: String, filename: String?, contentType: String): HttpEntity<Any> {
        val headerMap = LinkedMultiValueMap<String, String>()
        val builder: ContentDisposition.Builder = ContentDisposition
            .builder("form-data")
            .name(name)
        val contentDisposition: ContentDisposition =
            if (filename == null) builder.build() else builder.filename(filename).build()

        headerMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        headerMap.add(HttpHeaders.CONTENT_TYPE, contentType)
        return HttpEntity(body, headerMap)
    }
}
