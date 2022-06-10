package no.nav.sosialhjelp.soknad.innsending.digisosapi

import com.fasterxml.jackson.core.JsonProcessingException
import io.netty.channel.ChannelOption
import no.ks.fiks.streaming.klient.FilForOpplasting
import no.nav.sosialhjelp.kotlin.utils.logger
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.common.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.createHttpEntity
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.digisosObjectMapper
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.getDigisosIdFromResponse
import no.nav.sosialhjelp.soknad.innsending.digisosapi.Utils.stripVekkFnutter
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.io.IOException
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Future

interface DigisosApiV1Client {

    fun ping()

    fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String?
    ): String
}

class DigisosApiV1ClientImpl(
    private val digisosApiEndpoint: String,
    integrasjonsidFiks: String,
    integrasjonpassordFiks: String,
    private val kommuneInfoService: KommuneInfoService,
    private val dokumentlagerClient: DokumentlagerClient,
    private val krypteringService: KrypteringService,
    webClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient
) : DigisosApiV1Client {

    private val fiksWebClient = webClientBuilder
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(150 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(digisosObjectMapper))
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(digisosObjectMapper))
        }
        .defaultHeader(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
        .defaultHeader(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
        .build()

    override fun ping() {
        val kommuneInfo = kommuneInfoService.hentKommuneInfoFraFiks()
        check(kommuneInfo.isNotEmpty()) { "Fikk ikke kontakt med digisosapi" }
    }

    override fun krypterOgLastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        dokumenter: List<FilOpplasting>,
        kommunenr: String,
        navEksternRefId: String,
        token: String?
    ): String {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(dokumenter.size))
        val digisosId: String
        try {
            val fiksX509Certificate = dokumentlagerClient.getDokumentlagerPublicKeyX509Certificate()
            digisosId = lastOppFiler(
                soknadJson,
                tilleggsinformasjonJson,
                vedleggJson,
                dokumenter.map { dokument: FilOpplasting ->
                    FilForOpplasting.builder<Any>()
                        .filnavn(dokument.metadata.filnavn)
                        .metadata(dokument.metadata)
                        .data(krypteringService.krypter(dokument.data, krypteringFutureList, fiksX509Certificate))
                        .build()
                },
                kommunenr,
                navEksternRefId,
                token
            )
            waitForFutures(krypteringFutureList)
        } finally {
            krypteringFutureList
                .filter { !it.isDone && !it.isCancelled }
                .forEach { it.cancel(true) }
        }
        return digisosId
    }

    private fun lastOppFiler(
        soknadJson: String,
        tilleggsinformasjonJson: String,
        vedleggJson: String,
        filer: List<FilForOpplasting<Any>>,
        kommunenummer: String,
        behandlingsId: String,
        token: String?
    ): String {
        val body = LinkedMultiValueMap<String, Any>()
        body.add("tilleggsinformasjonJson", createHttpEntity(tilleggsinformasjonJson, "tilleggsinformasjonJson", null, APPLICATION_JSON_VALUE))
        body.add("soknadJson", createHttpEntity(soknadJson, "soknadJson", null, APPLICATION_JSON_VALUE))
        body.add("vedleggJson", createHttpEntity(vedleggJson, "vedleggJson", null, APPLICATION_JSON_VALUE))

        filer.forEachIndexed { index, fil ->
            body.add("metadata$index", createHttpEntity(getJson(fil), "metadata$index", null, TEXT_PLAIN_VALUE))
            body.add("dokument$index", createHttpEntity(InputStreamResource(fil.data), "dokument$index", fil.filnavn, APPLICATION_OCTET_STREAM_VALUE))
        }

        val startTime = System.currentTimeMillis()
        try {
            val response = fiksWebClient.post()
                .uri("$digisosApiEndpoint/digisos/api/v1/soknader/{kommunenummer}/{behandlingsId}", kommunenummer, behandlingsId)
                .header("requestid", UUID.randomUUID().toString())
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<String>()
                .block() ?: throw SosialhjelpSoknadApiException("Opplasting av $behandlingsId til fiks-digisos-api returnerte null -> kaster feil da vi forventer digisosId eller feilmelding")

            val digisosId = stripVekkFnutter(response)
            log.info("Sendte inn søknad $behandlingsId til kommune $kommunenummer og fikk digisosid: $digisosId")
            return digisosId
        } catch (e: WebClientResponseException) {
            val errorResponse = e.responseBodyAsString
            val fiksDigisosId = getDigisosIdFromResponse(errorResponse, behandlingsId)
            if (fiksDigisosId != null) {
                log.warn("Søknad $behandlingsId er allerede sendt til fiks-digisos-api med id $fiksDigisosId. Returner digisos-id som normalt så brukeren blir rutet til innsyn. ErrorResponse var: $errorResponse")
                return fiksDigisosId
            }
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet etter ${System.currentTimeMillis() - startTime} ms med status ${e.statusCode} og response: $errorResponse")
        } catch (e: IOException) {
            throw IllegalStateException("Opplasting av $behandlingsId til fiks-digisos-api feilet", e)
        }
    }

    private fun getJson(objectFilForOpplasting: FilForOpplasting<Any>): String {
        return try {
            digisosObjectMapper.writeValueAsString(objectFilForOpplasting.metadata)
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e)
        }
    }

    companion object {
        private val log by logger()

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
