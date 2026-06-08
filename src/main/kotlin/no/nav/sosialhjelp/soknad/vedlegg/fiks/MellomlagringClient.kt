package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.KrypteringService.Companion.waitForFutures
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilMetadata
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.io.ByteArrayInputStream
import java.util.Collections
import java.util.concurrent.Future

interface MellomlagringClient {
    fun hentDokumenterMetadata(navEksternId: String): MellomlagringDto?

    fun lastOppDokument(
        navEksternId: String,
        filnavn: String,
        data: ByteArray,
    ): MellomlagringDto

    fun slettAlleDokumenter(navEksternId: String)

    fun hentDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ): ByteArray

    fun slettDokument(
        navEksternId: String,
        digisosDokumentId: String,
    )
}

class MellomlagringClientImpl(
    private val krypteringService: KrypteringService,
    private val texasService: TexasService,
    private val webClient: WebClient,
) : MellomlagringClient {
    @WithSpan("Get Metadata for Documents as Fiks Mellomlager")
    override fun hentDokumenterMetadata(navEksternId: String): MellomlagringDto? =
        runCatching { doHentDokumenterMetadata(navEksternId) }
            .getOrElse {
                if (it is WebClientResponseException.NotFound) {
                    return null
                } else {
                    Span.current().recordException(it)
                    Span.current().setStatus(StatusCode.ERROR)
                    when (it) {
                        is WebClientResponseException -> {
                            logger.error(
                                "Fiks - getMellomlagredeVedlegg feilet: ${it.statusCode} -> ${it.responseBodyAsString}",
                                it,
                            )
                        }
                        else -> logger.error("Fiks - getMellomlagredeVedlegg feilet", it)
                    }
                    throw it
                }
            }

    private fun doHentDokumenterMetadata(navEksternId: String): MellomlagringDto {
        return webClient.get()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getToken())
            .retrieve()
            .bodyToMono<MellomlagringDto>()
            .block() ?: throw FiksException("MellomlagringDto er null?", null)
    }

    @WithSpan("Upload Document to Fiks Mellomlager")
    override fun lastOppDokument(
        navEksternId: String,
        filnavn: String,
        data: ByteArray,
    ): MellomlagringDto {
        return lastOppDokument(
            navEksternId = navEksternId,
            filOpplasting =
                FilOpplasting(
                    metadata =
                        FilMetadata(
                            filnavn = filnavn,
                            mimetype = "application/octet-stream",
                            storrelse = data.size.toLong(),
                        ),
                    data = ByteArrayInputStream(data),
                ),
        )
    }

    /**
     * Last opp vedlegg til mellomlagring for `navEksternId`
     */
    private fun lastOppDokument(
        navEksternId: String,
        filOpplasting: FilOpplasting,
    ): MellomlagringDto {
        val krypteringFutureList = Collections.synchronizedList(ArrayList<Future<Void>>(1))

        return runCatching {
            doUploadDocument(
                navEksternId = navEksternId,
                filForOpplasting = filOpplasting.krypterData(krypteringFutureList),
            )
                .also { waitForFutures(krypteringFutureList) }
        }
            .getOrElse {
                Span.current().recordException(it)
                Span.current().setStatus(StatusCode.ERROR)
                throw FiksException("Feil ved opplasting av dokument", it)
            }
            .also {
                krypteringFutureList
                    .filter { !it.isDone && !it.isCancelled }
                    .forEach { it.cancel(true) }
            }
    }

    private fun FilOpplasting.krypterData(krypteringFutureList: MutableList<Future<Void>>): FilOpplasting =
        copy(data = krypteringService.krypter(data, krypteringFutureList))

    private fun doUploadDocument(
        filForOpplasting: FilOpplasting,
        navEksternId: String,
    ): MellomlagringDto {
        val body = createBodyForUpload(filForOpplasting)

        val startTime = System.currentTimeMillis()
        return webClient.post()
            .uri(MELLOMLAGRING_PATH, navEksternId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getToken())
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .bodyToMono<MellomlagringDto>()
            .doOnError(WebClientResponseException::class.java) {
                // TODO Logging utenfor MDC Context
                logger.warn(
                    "Mellomlagring av vedlegg til søknad $navEksternId feilet etter ${System.currentTimeMillis() - startTime} ms med status ${it.statusCode} og response: ${it.responseBodyAsString}",
                    it,
                )
            }
            .block()
            ?: throw FiksException("MellomlagringDto er null ved opplasting av dokument", null)
    }

    /**
     * Slett alle mellomlagrede vedlegg for `navEksternId`
     */
    @WithSpan("Delete all documents for Soknad in Fiks Mellomlager")
    override fun slettAlleDokumenter(navEksternId: String) {
        runCatching {
            webClient.delete()
                .uri(MELLOMLAGRING_PATH, navEksternId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + getToken())
                .retrieve()
                .bodyToMono<String>()
                .block()
        }
            .onFailure {
                Span.current().recordException(it)
                Span.current().setStatus(StatusCode.ERROR)

                if (it is WebClientResponseException) {
                    logger.warn("Fiks - deleteAll mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
                }

                throw it
            }
    }

    /**
     * Last ned mellomlagret vedlegg
     */
    @WithSpan("Get Document from Fiks Mellomlager")
    override fun hentDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ): ByteArray =
        runCatching {
            webClient.get()
                .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + getToken())
                .retrieve()
                .bodyToMono<ByteArray>()
                .block()
                ?: throw FiksException("Mellomlagret vedlegg er null?", null)
        }
            .getOrElse {
                Span.current().recordException(it)
                Span.current().setStatus(StatusCode.ERROR)
                throw it
            }

    /**
     * Slett mellomlagret vedlegg
     */
    @WithSpan("Delete Document from Fiks Mellomlager")
    override fun slettDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ) {
        runCatching {
            webClient.delete()
                .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + getToken())
                .retrieve()
                .bodyToMono<String>()
                .block()
        }
            .onSuccess { logger.info("Fiks - delete mellomlagretVedlegg OK. vedleggId=$digisosDokumentId") }
            .onFailure {
                Span.current().recordException(it)
                Span.current().setStatus(StatusCode.ERROR)
                if (it is WebClientResponseException) logger.warn("Fiks - delete mellomlagretVedlegg feilet - ${it.responseBodyAsString}", it)
                throw it
            }
    }

    private fun getToken(): String = texasService.getToken(IdentityProvider.M2M, "ks:fiks")

    private fun createBodyForUpload(file: FilOpplasting): MultiValueMap<String, HttpEntity<*>> =
        MultipartBodyBuilder()
            .run {
                part("metadata", createJsonFilMetadata(file.metadata))
                    .contentType(MediaType.APPLICATION_JSON)

                part("files", InputStreamResource(file.data))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .filename(file.metadata.filnavn)

                build()
            }

    private fun createJsonFilMetadata(metadata: FilMetadata): String =
        objectMapper.writeValueAsString(
            FilMetadata(
                filnavn = metadata.filnavn,
                mimetype = metadata.mimetype,
                storrelse = metadata.storrelse,
            ),
        )

    companion object {
        private const val MELLOMLAGRING_PATH = "digisos/api/v1/mellomlagring/{navEksternRefId}"
        private const val MELLOMLAGRING_DOKUMENT_PATH =
            "digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}"

        private val objectMapper = jacksonObjectMapper()

        private val logger by logger()
    }
}
