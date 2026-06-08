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
                .retrieve()
                .bodyToMono<KontoDto>()
                .map<KontoResponse> { dto -> KontoResponse.Success(dto) }
                .onErrorResume { e -> Mono.just(KontoResponse.Error(e)) }
                .awaitSingle()
        }

    private suspend fun getTokenX(personId: String) =
        texasService.exchangeToken(personId, IdentityProvider.TOKENX, kontoregisterAudience)
}

sealed interface KontoResponse {
    data class Success(val kontoDto: KontoDto) : KontoResponse

    data class Error(val throwable: Throwable) : KontoResponse
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
