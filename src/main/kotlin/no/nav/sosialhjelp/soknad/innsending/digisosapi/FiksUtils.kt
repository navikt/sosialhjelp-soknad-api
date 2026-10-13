package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.client.config.fiksServiceConnectionProvider
import no.nav.sosialhjelp.soknad.app.filter.MdcExchangeFilter
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.Duration
import java.util.UUID

object FiksUtils {
    val sosialhjelpJsonMapper: JsonMapper =
        JsonSosialhjelpObjectMapper
            .createJsonMapperBuilder()
            .addModule(kotlinModule())
            .build()

    fun isAlleredeMottatt(
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

    fun isMottakPabegynt(
        errorMessage: String,
        soknadId: UUID,
    ): Boolean {
        listOf("Mottak av søknad", soknadId.toString(), "navExternRefId", "er allerede påbegynt")
            .forEach { if (!errorMessage.contains(it)) return false }

        return true
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

    private val ONE_MINUTE = Duration.ofMinutes(1)
    private val TWO_MINUTES = Duration.ofMinutes(2)

    private val fiksHttpClient =
        HttpClient.create(fiksServiceConnectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ONE_MINUTE.toMillis().toInt())
            .doOnConnected {
                it
                    .addHandlerLast(ReadTimeoutHandler(ONE_MINUTE.toSeconds().toInt()))
                    .addHandlerLast(WriteTimeoutHandler(ONE_MINUTE.toSeconds().toInt()))
            }
            .responseTimeout(TWO_MINUTES)

    fun createFiksWebClient(
        webClientBuilder: WebClient.Builder,
        integrasjonsidFiks: String,
        integrasjonpassordFiks: String
    ) = webClientBuilder
        .clientConnector(ReactorClientHttpConnector(fiksHttpClient))
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            it.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(sosialhjelpJsonMapper))
            it.defaultCodecs().jacksonJsonDecoder(JacksonJsonDecoder(sosialhjelpJsonMapper))
        }
        .defaultHeader(Constants.HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(Constants.HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .filter(MdcExchangeFilter)
        .build()

    fun createBody(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filer: List<FilOpplasting>,
    ): LinkedMultiValueMap<String, Any> {
        val body = LinkedMultiValueMap<String, Any>()
        body.add(
            "tilleggsinformasjonJson",
            createHttpEntity(tilleggsinformasjonJson, "tilleggsinformasjonJson", null, APPLICATION_JSON_VALUE),
        )
        body.add("soknadJson", createHttpEntity(soknadJson, "soknadJson", null, APPLICATION_JSON_VALUE))
        body.add("vedleggJson", createHttpEntity(vedleggJson, "vedleggJson", null, APPLICATION_JSON_VALUE))

        filer.forEachIndexed { index, fil ->
            body.add("metadata$index", createHttpEntity(getJson(fil), "metadata$index", null, TEXT_PLAIN_VALUE))
            body.add(
                fil.metadata.filnavn,
                createHttpEntity(
                    ByteArrayResource(IOUtils.toByteArray(fil.data)),
                    fil.metadata.filnavn,
                    fil.metadata.filnavn,
                    APPLICATION_OCTET_STREAM_VALUE,
                ),
            )
        }
        return body
    }

    private fun getJson(objectFilForOpplasting: FilOpplasting): String = sosialhjelpJsonMapper.writeValueAsString(objectFilForOpplasting.metadata)
}
