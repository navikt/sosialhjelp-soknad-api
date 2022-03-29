package no.nav.sosialhjelp.soknad.innsending.svarut

import no.nav.sosialhjelp.soknad.common.rest.RestConfig
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.health.selftest.Pingable
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtClient
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtClientImpl
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import java.nio.charset.StandardCharsets
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter
import javax.xml.bind.DatatypeConverter

@Configuration
open class SvarUtConfig(
    @Value("\${svarut_url}") private var baseurl: String,
    @Value("\${fiks_svarut_username}") private val svarutUsername: String?,
    @Value("\${fiks_svarut_password}") private val svarutPassword: String?
) {

    @Bean
    open fun svarUtClient(): SvarUtClient {
        return SvarUtClientImpl(client, baseurl)
    }

    @Bean
    open fun svarUtPing(svarUtClient: SvarUtClient): Pingable {
        return Pingable {
            val metadata = Pingable.PingMetadata(baseurl, "SvarUt", false)
            try {
                svarUtClient.ping()
                Pingable.lyktes(metadata)
            } catch (e: Exception) {
                Pingable.feilet(metadata, e)
            }
        }
    }

    private val client: Client
        get() {
            val config = RestConfig(connectTimeout = SVARUT_TIMEOUT, readTimeout = SVARUT_TIMEOUT)
            return RestUtils
                .createClient(config)
                .register(MultiPartFeature::class.java)
                .register(ClientRequestFilter { it.headers.putSingle(AUTHORIZATION, basicAuthentication) })
        }

    private val basicAuthentication: String
        get() {
            if (svarutUsername == null || svarutPassword == null) {
                throw RuntimeException("svarutUsername eller svarutPassword er ikke tilgjengelig.")
            }
            val token = "$svarutUsername:$svarutPassword"
            return "Basic " + DatatypeConverter.printBase64Binary(token.toByteArray(StandardCharsets.UTF_8))
        }

    companion object {
        private const val SVARUT_TIMEOUT = 16 * 60 * 1000
    }
}
