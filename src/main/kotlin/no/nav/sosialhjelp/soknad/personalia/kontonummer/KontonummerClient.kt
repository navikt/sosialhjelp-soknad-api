package no.nav.sosialhjelp.soknad.personalia.kontonummer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import no.nav.sosialhjelp.soknad.app.Constants.BEARER
import no.nav.sosialhjelp.soknad.app.client.config.configureWebClientBuilder
import no.nav.sosialhjelp.soknad.app.client.config.createDefaultHttpClient
import no.nav.sosialhjelp.soknad.auth.texas.IdentityProvider
import no.nav.sosialhjelp.soknad.auth.texas.NonBlockingTexasService
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

interface KontonummerClient {
    suspend fun getKontonummer(): KontoResponse
}

@Component
class KontonummerClientImpl(
    @param:Value("\${kontoregister_api_baseurl}") private val kontoregisterUrl: String,
    @param:Value("\${kontoregister_api_audience}") private val kontoregisterAudience: String,
    private val texasService: NonBlockingTexasService,
    webClientBuilder: WebClient.Builder,
) : KontonummerClient {
    private val webClient =
        webClientBuilder.configureWebClientBuilder(createDefaultHttpClient()).build()

    override suspend fun getKontonummer(): KontoResponse =
        withContext(Dispatchers.IO) {
            webClient.get()
                .uri("$kontoregisterUrl/api/borger/v1/hent-aktiv-konto")
                .header(AUTHORIZATION, BEARER + getTokenX(currentUserContext().userToken))
                .exchangeToMono { handleResponse(it) }
                .onErrorResume { e -> Mono.just(KontoResponse.Error(throwable = e)) }
                .awaitSingle()
        }

    private fun handleResponse(response: ClientResponse): Mono<KontoResponse> =
        response.statusCode().let { code ->
            when {
                code.is2xxSuccessful -> {
                    response.bodyToMono<KontoDto>()
                        .map<KontoResponse> { dto -> KontoResponse.Success(dto) }
                        .switchIfEmpty(Mono.just(KontoResponse.Null))
                }
                code.value() == 404 -> {
                    Mono.just(KontoResponse.Error(code.value()))
                }
                else -> response.createException().map { e -> KontoResponse.Error(code.value(), e) }
            }
        }

    private suspend fun getTokenX(personId: String) =
        texasService.exchangeToken(personId, IdentityProvider.TOKENX, kontoregisterAudience)
}

sealed interface KontoResponse {
    data class Success(val kontoDto: KontoDto) : KontoResponse

    data class Error(val statusCode: Int? = null, val throwable: Throwable? = null) : KontoResponse

    object Null : KontoResponse
}

data class KontoDto(
    val kontonummer: String,
    val utenlandskKontoInfo: UtenlandskKontoInfo?,
)

data class UtenlandskKontoInfo(
    val banknavn: String?,
    val bankkode: String?,
    val bankLandkode: String,
    val valutakode: String,
    val swiftBicKode: String?,
    val bankadresse1: String?,
    val bankadresse2: String?,
    val bankadresse3: String?,
)
