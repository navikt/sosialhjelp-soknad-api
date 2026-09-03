package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.ErrorMessage
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.client.config.RetryUtils
import no.nav.sosialhjelp.soknad.app.client.config.fiksServiceConnectionProvider
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.app.filter.MdcExchangeFilter
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.sosialhjelpJsonMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.time.Duration
import java.util.UUID

@Component
class DigisosApiV2Client(
    @param:Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @param:Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @param:Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val texasService: TexasService,
    webClientBuilder: WebClient.Builder,
) {
    private val fiksHttpClient =
        HttpClient.create(fiksServiceConnectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ONE_MINUTE.toMillis().toInt())
            .doOnConnected {
                it
                    .addHandlerLast(ReadTimeoutHandler(ONE_MINUTE.toSeconds().toInt()))
                    .addHandlerLast(WriteTimeoutHandler(ONE_MINUTE.toSeconds().toInt()))
            }
            .responseTimeout(TWO_MINUTES)

    private val fiksWebClient =
        webClientBuilder
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

    fun lastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filer: List<FilOpplasting>,
        kommunenummer: String,
        soknadId: UUID,
    ): SendSoknadResponse {
        val body = createBody(soknadJson, tilleggsinformasjonJson, vedleggJson, filer)

        return runCatching {
            doLastOppFiler(
                soknadId = soknadId,
                kommunenummer = kommunenummer,
                body = body,
            )
                .let { SendSoknadResponse.Success(it) }
        }
            .getOrElse { e ->
                when (val errorMessage = e.toFiksErrorMessageOrNull()) {
                    null -> SendSoknadResponse.Error(e)
                    else -> SendSoknadResponse.FiksError(errorMessage, e)
                }
            }
    }

    private fun doLastOppFiler(
        soknadId: UUID,
        kommunenummer: String,
        body: LinkedMultiValueMap<String, Any>,
    ): UUID {
        return fiksWebClient
            .post()
            .uri("$digisosApiEndpoint/digisos/api/v2/soknader/{kommunenummer}/{behandlingsId}", kommunenummer, soknadId)
            .header(AUTHORIZATION, "Bearer $userToken")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono<String>()
            .block()
            // digisosId
            ?.let { UUID.fromString(Utils.stripVekkFnutter(it)) }
            ?: error("Fiks - noe uventet feilet ved innsending av søknad. Response er null?")
    }

    private fun createBody(
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

    fun getSoknader(): List<DigisosSak> {
        val startTime = System.currentTimeMillis()
        return try {
            fiksWebClient
                .get()
                .uri("$digisosApiEndpoint/digisos/api/v1/soknader/soknader")
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $userToken")
                .retrieve()
                .bodyToMono<List<DigisosSak>>()
                .block()
                ?: throw FiksException(
                    message = "Fiks - noe uventet feilet ved henting av søknader. Response er null?",
                    cause = null,
                )
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            throw IllegalStateException("Henting av søknader hos Fiks feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Henting av søknader hos Fiks feilet", e)
        }
    }

    fun getInnsynsfil(
        digisosId: String,
        dokumentLagerId: String,
    ): JsonDigisosSoker {
        val startTime = System.currentTimeMillis()
        return try {
            fiksWebClient
                .get()
                .uri(
                    "$digisosApiEndpoint/digisos/api/v1/soknader/{digisosId}/dokumenter/{dokumentlagerId}",
                    digisosId,
                    dokumentLagerId,
                )
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "Bearer $userToken")
                .retrieve()
                .bodyToMono<JsonDigisosSoker>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?: throw FiksException("Fiks - noe uventet feilet ved henting av innsynsfil. Response er null?", null)
        } catch (e: WebClientResponseException) {
            val errorResponse = sosialhjelpJsonMapper.readValue(e.responseBodyAsString, ErrorMessage::class.java)
            throw IllegalStateException("Henting av innsynsfil hos Fiks feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Henting av innsynsfil hos Fiks feilet", e)
        }
    }

    fun getStatusForSoknader(
        digisosIdListe: List<UUID>,
    ): FiksSoknadStatusListe {
        val startTime = System.currentTimeMillis()

        val sporingsId = UUID.randomUUID().toString()
        val fiksSoknaderStatusRequest = FiksSoknaderStatusRequest(digisosIdListe)

        return try {
            fiksWebClient
                .post()
                .uri("$digisosApiEndpoint/digisos/api/v1/nav/soknader/status".plus("?sporingsId=$sporingsId"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, "BEARER $maskinportenToken")
                .bodyValue(fiksSoknaderStatusRequest)
                .retrieve()
                .bodyToMono<FiksSoknadStatusListe>()
                .retryWhen(RetryUtils.DEFAULT_RETRY_SERVER_ERRORS)
                .block()
                ?: throw FiksException(
                    message = "Fiks - noe uventet feilet ved henting av status for søknader. Response er null?",
                    cause = null,
                )
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            throw IllegalStateException(
                "Henting av status for søknader hos Fiks feilet etter " +
                    "${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} " +
                    "og response: $errorResponse. SporingsId: $sporingsId",
            )
        } catch (e: IOException) {
            throw IllegalStateException("Henting av status for søknader hos Fiks feilet. SporingsId: $sporingsId", e)
        }
    }

    private fun getJson(objectFilForOpplasting: FilOpplasting): String =
        try {
            sosialhjelpJsonMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }

    private val maskinportenToken get() = texasService.getToken(IdentityProvider.M2M, "ks:fiks")
    private val userToken get() = SubjectHandlerUtils.getTokenOrNull() ?: error("Mangler userToken")

    companion object {
        private val ONE_MINUTE = Duration.ofMinutes(1)
        private val TWO_MINUTES = Duration.ofMinutes(2)
    }
}

private fun Throwable.toFiksErrorMessageOrNull(): ErrorMessage? =
    runCatching {
        when (this) {
            is WebClientResponseException -> jacksonObjectMapper().readValue(responseBodyAsString, ErrorMessage::class.java)
            else -> null
        }
    }.getOrNull()

sealed interface SendSoknadResponse {
    class Success(val digisosId: UUID) : SendSoknadResponse

    class FiksError(val errorMessage: ErrorMessage, val e: Throwable) : SendSoknadResponse

    class Error(val e: Throwable) : SendSoknadResponse
}

data class FiksSoknaderStatusRequest(
    val digisosIdListe: List<UUID>,
)

data class FiksSoknadStatusListe(
    val statusListe: List<FiksSoknadStatus>,
)

data class FiksSoknadStatus(
    val digisosId: UUID,
    val levertFagsystem: Boolean,
)

class AlleredeMottattException(
    val digisosId: UUID,
    message: String,
) : SosialhjelpSoknadApiException(message)
