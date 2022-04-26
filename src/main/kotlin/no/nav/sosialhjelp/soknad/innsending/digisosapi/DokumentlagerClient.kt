package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.netty.channel.ChannelOption
import no.nav.sosialhjelp.soknad.auth.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.Constants.BEARER
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.common.Constants.HEADER_INTEGRASJON_PASSORD
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.io.ByteArrayInputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Duration
import javax.ws.rs.core.MediaType

interface DokumentlagerClient {
    fun getDokumentlagerPublicKeyX509Certificate(): X509Certificate
}

@Component
class DokumentlagerClientImpl(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    @Value("\${integrasjonsid_fiks}") private val integrasjonsidFiks: String,
    @Value("\${integrasjonpassord_fiks}") private val integrasjonpassordFiks: String,
    private val maskinportenClient: MaskinportenClient,
    proxiedWebClientBuilder: WebClient.Builder,
    proxiedHttpClient: HttpClient,
) : DokumentlagerClient {

    private var cachedPublicKey: X509Certificate? = null

    private val fiksWebClient = proxiedWebClientBuilder
        .baseUrl(digisosApiEndpoint)
        .clientConnector(
            ReactorClientHttpConnector(
                proxiedHttpClient
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SENDING_TIL_FIKS_TIMEOUT)
                    .responseTimeout(Duration.ofMillis(SENDING_TIL_FIKS_TIMEOUT.toLong()))
            )
        )
        .codecs {
            it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(Utils.digisosObjectMapper))
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(Utils.digisosObjectMapper))
        }
        .build()

    override fun getDokumentlagerPublicKeyX509Certificate(): X509Certificate {
        cachedPublicKey?.let { return it }

        val publicKey = fiksWebClient.get()
            .uri("/digisos/api/v1/dokumentlager-public-key")
            .header(ACCEPT, MediaType.WILDCARD)
            .header(HEADER_INTEGRASJON_ID, integrasjonsidFiks)
            .header(HEADER_INTEGRASJON_PASSORD, integrasjonpassordFiks)
            .header(AUTHORIZATION, BEARER + maskinportenClient.getToken())
            .retrieve()
            .bodyToMono<ByteArray>()
            .onErrorMap(WebClientResponseException::class.java) { e ->
                log.warn("Fiks - getDokumentlagerPublicKey feilet - ${e.statusCode} ${e.statusText}", e)
                TjenesteUtilgjengeligException("Noe feilet ved henting av dokumentlager publickey fra Fiks - ${e.message}", e)
            }
            .block()

        log.info("Hentet public key for dokumentlager")

        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            return (certificateFactory.generateCertificate(ByteArrayInputStream(publicKey)) as X509Certificate)
                .also { cachedPublicKey = it }
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val log = getLogger(DokumentlagerClientImpl::class.java)

        private const val SENDING_TIL_FIKS_TIMEOUT = 5 * 60 * 1000 // 5 minutter
    }
}
