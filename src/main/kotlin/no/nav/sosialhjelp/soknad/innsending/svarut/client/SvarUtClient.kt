package no.nav.sosialhjelp.soknad.innsending.svarut.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.netty.channel.ChannelOption
import jakarta.xml.bind.DatatypeConverter
import no.ks.fiks.svarut.klient.model.Forsendelse
import no.ks.fiks.svarut.klient.model.ForsendelsesId
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.client.config.mdcExchangeFilter
import no.nav.sosialhjelp.soknad.app.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.Duration

@Component
class SvarUtClient(
    @Value("\${svarut_url}") private var baseurl: String,
    @Value("\${fiks_svarut_username}") private val svarutUsername: String?,
    @Value("\${fiks_svarut_password}") private val svarutPassword: String?,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) {

    private val basicAuthentication: String
        get() {
            if (svarutUsername == null || svarutPassword == null) {
                throw RuntimeException("svarutUsername eller svarutPassword er ikke tilgjengelig.")
            }
            val token = "$svarutUsername:$svarutPassword"
            return "Basic " + DatatypeConverter.printBase64Binary(token.toByteArray(StandardCharsets.UTF_8))
        }

    private val svarUtWebClient = webClientBuilder
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SVARUT_TIMEOUT)
                    .responseTimeout(Duration.ofMillis(SVARUT_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(150 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
        }
        .defaultHeader(AUTHORIZATION, basicAuthentication)
        .filter(mdcExchangeFilter)
        .build()

    fun ping() {
        svarUtWebClient.get()
            .uri("$baseurl/tjenester/api/forsendelse/v1/forsendelseTyper")
            .retrieve()
            .bodyToMono<String>()
            .doOnError(WebClientResponseException::class.java) {
                log.warn("Ping feilet mot SvarUt. ${it.statusCode}", it)
            }
            .block()
    }

    fun sendForsendelse(forsendelse: Forsendelse, data: Map<String, InputStream>): ForsendelsesId? {
        return try {
            val body = LinkedMultiValueMap<String, Any>()
            body.add("forsendelse", createHttpEntity(objectMapper.writeValueAsString(forsendelse), "forsendelse", null, MediaType.APPLICATION_JSON_VALUE))
            forsendelse.dokumenter.forEach {
                body.add("filer", createHttpEntity(ByteArrayResource(IOUtils.toByteArray(data[it.filnavn])), "filer", it.filnavn, MediaType.APPLICATION_OCTET_STREAM_VALUE))
            }

            svarUtWebClient.post()
                .uri("$baseurl/tjenester/api/forsendelse/v1/sendForsendelse")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<ForsendelsesId>()
                .block()
        } catch (e: WebClientResponseException) {
            log.warn("Noe feilet ved kall til SvarUt (rest) - ${e.statusCode} ${e.responseBodyAsString}", e)
            throw e
        } catch (e: Exception) {
            throw TjenesteUtilgjengeligException("Noe feilet ved kall til SvarUt (rest)", e)
        }
    }

    companion object {
        private val log by logger()
        private val objectMapper = jacksonObjectMapper()

        private const val SVARUT_TIMEOUT = 16 * 60 * 1000
    }
}
