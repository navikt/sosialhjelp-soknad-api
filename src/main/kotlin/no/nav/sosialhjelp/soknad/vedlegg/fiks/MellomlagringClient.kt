package no.nav.sosialhjelp.soknad.vedlegg.fiks

import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.TexasService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

interface MellomlagringClient {
    fun hentDokumenterMetadata(navEksternId: String): MellomlagringDto?

    fun hentDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ): ByteArray
}

class MellomlagringClientImpl(
    private val texasService: TexasService,
    private val webClient: WebClient,
) : MellomlagringClient {
    override fun hentDokumenterMetadata(navEksternId: String): MellomlagringDto? =
        runCatching { doHentDokumenterMetadata(navEksternId) }
            .getOrElse {
                when (it) {
                    is WebClientResponseException.NotFound -> return null
                    is WebClientResponseException ->
                        logger.error(
                            "Fiks - getMellomlagredeVedlegg feilet: ${it.statusCode} -> ${it.responseBodyAsString}",
                            it,
                        )

                    else -> logger.error("Fiks - getMellomlagredeVedlegg feilet ukjent feil: ${it.message}", it)
                }
                throw it
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

    /**
     * Last ned mellomlagret vedlegg
     */
    override fun hentDokument(
        navEksternId: String,
        digisosDokumentId: String,
    ): ByteArray {
        return webClient.get()
            .uri(MELLOMLAGRING_DOKUMENT_PATH, navEksternId, digisosDokumentId)
            .header(HttpHeaders.AUTHORIZATION, BEARER + getToken())
            .retrieve()
            .bodyToMono<ByteArray>()
            .block()
            ?: throw FiksException("Mellomlagret vedlegg er null?", null)
    }

    private fun getToken(): String = texasService.getToken(IdentityProvider.M2M, "ks:fiks")

    companion object {
        private const val MELLOMLAGRING_PATH = "digisos/api/v1/mellomlagring/{navEksternRefId}"
        private const val MELLOMLAGRING_DOKUMENT_PATH =
            "digisos/api/v1/mellomlagring/{navEksternRefId}/{digisosDokumentId}"

        private val logger by logger()
    }
}
