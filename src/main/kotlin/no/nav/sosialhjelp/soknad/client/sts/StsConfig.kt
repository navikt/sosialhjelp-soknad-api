package no.nav.sosialhjelp.soknad.client.sts

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata
import org.eclipse.jetty.http.HttpHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter
import javax.xml.bind.DatatypeConverter

@Configuration
open class StsConfig(
    @Value("\${srvsoknadsosialhjelp.server.username}") private val srvsoknadsosialhjelpServerUsername: String?,
    @Value("\${srvsoknadsosialhjelp.server.password}") private val srvsoknadsosialhjelpServerPassword: String?,
    @Value("\${securitytokenservice_apigw_url}") private val baseurl: String
) {

    @Bean
    open fun stsClient(): StsClient {
        return StsClientImpl(client, baseurl)
    }

    @Bean
    open fun stsPing(stsClient: StsClient): Pingable {
        return Pingable {
            val metadata = PingMetadata(baseurl, "STSClient", false)
            try {
                stsClient.ping()
                Ping.lyktes(metadata)
            } catch (e: Exception) {
                Ping.feilet(metadata, e)
            }
        }
    }

    private val client: Client = RestUtils.createClient()
        .register(ClientRequestFilter { it.headers.putSingle(HttpHeader.AUTHORIZATION.toString(), basicAuthentication) })
        .register(ClientRequestFilter { it.headers.putSingle(HeaderConstants.HEADER_NAV_APIKEY, System.getenv(STSTOKEN_APIKEY)) })

    private val basicAuthentication: String
        get() {
            if (srvsoknadsosialhjelpServerUsername == null || srvsoknadsosialhjelpServerPassword == null) {
                throw RuntimeException("Username eller password er ikke tilgjengelig.")
            }
            val token = "$srvsoknadsosialhjelpServerUsername:$srvsoknadsosialhjelpServerPassword"
            return "Basic " + DatatypeConverter.printBase64Binary(token.toByteArray(StandardCharsets.UTF_8))
        }

    companion object {
        private const val STSTOKEN_APIKEY = "STSTOKEN_APIKEY"
    }
}
