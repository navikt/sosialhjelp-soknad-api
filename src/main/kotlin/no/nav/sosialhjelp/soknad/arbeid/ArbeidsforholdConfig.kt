package no.nav.sosialhjelp.soknad.arbeid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
open class ArbeidsforholdConfig(
    @Value("\${aareg_api_baseurl}") private val baseurl: String,
    private val stsConsumer: STSConsumer
) {

    @Bean
    open fun arbeidsforholdClient(): ArbeidsforholdClient {
        return ArbeidsforholdClientImpl(arbeidsforholdClient, baseurl, stsConsumer)
    }

    @Bean
    open fun arbeidsforholdPing(arbeidsforholdClient: ArbeidsforholdClient): Pingable {
        return Pingable {
            val metadata = PingMetadata(baseurl, "Aareg", false)
            try {
                arbeidsforholdClient.ping()
                Ping.lyktes(metadata)
            } catch (e: Exception) {
                Ping.feilet(metadata, e)
            }
        }
    }

    private val arbeidsforholdClient: Client
        get() = RestUtils.createClient()
            .register(arbeidsforholdMapper)
            .register(
                ClientRequestFilter { it.headers.putSingle(HEADER_NAV_APIKEY, System.getenv(AAREGAPI_APIKEY)) }
            )

    private val arbeidsforholdMapper: ObjectMapper
        get() = jacksonObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(JavaTimeModule())

    companion object {
        private const val AAREGAPI_APIKEY = "AAREGAPI_APIKEY"
    }
}