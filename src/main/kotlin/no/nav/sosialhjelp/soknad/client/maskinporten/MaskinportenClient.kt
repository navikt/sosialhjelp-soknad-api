package no.nav.sosialhjelp.soknad.client.maskinporten

import com.nimbusds.jwt.SignedJWT
import no.nav.sosialhjelp.soknad.client.maskinporten.dto.MaskinportenResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

interface MaskinportenClient {
    fun getTokenString(): String
}

class MaskinportenClientImpl(
    private val webClient: WebClient,
    maskinportenConfig: MaskinportenConfig,
    private val wellKnown: WellKnown
) : MaskinportenClient {

    private var tokenCache = TokenCache()
    private val tokenGenerator = MaskinportenGrantTokenGenerator(maskinportenConfig, wellKnown.issuer)

    override fun getTokenString(): String {
        return getTokenFraCache().parsedString
    }

    private fun getTokenFraCache(): SignedJWT {
        return tokenCache.getToken() ?: TokenCache(getTokenFraMaskinporten().access_token)
            .also { tokenCache = it }
            .getToken()!!
    }

    private fun getTokenFraMaskinporten(): MaskinportenResponse {
        val response = webClient.post()
            .uri(wellKnown.token_endpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(params))
            .retrieve()
            .bodyToMono<MaskinportenResponse>()
            .doOnSuccess { log.info("Hentet token fra Maskinporten") }
            .doOnError { log.warn("Noe feilet ved henting av token fra Maskinporten", it) }
            .block()

        return response!!
    }

    private val params: MultiValueMap<String, String>
        get() = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", GRANT_TYPE)
            add("assertion", tokenGenerator.getJwt())
        }

    companion object {
        private const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"
        private val log = LoggerFactory.getLogger(MaskinportenClientImpl::class.java)
    }

}
