package no.nav.sosialhjelp.soknad.innsending.digisosapi.dokumentlager

import no.nav.sosialhjelp.soknad.client.exceptions.TjenesteUtilgjengeligException
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_INTEGRASJON_ID
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_INTEGRASJON_PASSORD
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiProperties
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.ByteArrayInputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.ws.rs.core.MediaType

interface DokumentlagerClient {
    fun getDokumentlagerPublicKeyX509Certificate(token: String?): X509Certificate
}

class DokumentlagerClientImpl(
    private val fiksWebClient: WebClient,
    private val properties: DigisosApiProperties
) : DokumentlagerClient {

    private var cachedPublicKey: X509Certificate? = null

    override fun getDokumentlagerPublicKeyX509Certificate(token: String?): X509Certificate {
        cachedPublicKey?.let { return it }

        val publicKey = fiksWebClient.get()
            .uri("/digisos/api/v1/dokumentlager-public-key")
            .header(ACCEPT, MediaType.WILDCARD)
            .header(HEADER_INTEGRASJON_ID, properties.integrasjonsidFiks)
            .header(HEADER_INTEGRASJON_PASSORD, properties.integrasjonpassordFiks)
            .header(AUTHORIZATION, token)
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
    }
}
