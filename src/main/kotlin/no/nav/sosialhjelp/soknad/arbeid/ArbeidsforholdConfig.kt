package no.nav.sosialhjelp.soknad.arbeid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.client.sts.StsClient
import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils
import no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientRequestFilter

@Configuration
@Import(ArbeidRessurs::class)
open class ArbeidsforholdConfig(
    @Value("\${aareg_api_baseurl}") private val baseurl: String,
    private val stsClient: StsClient,
    private val organisasjonService: OrganisasjonService
) {

    @Bean
    open fun arbeidsforholdService(arbeidsforholdClient: ArbeidsforholdClient): ArbeidsforholdService {
        return ArbeidsforholdService(arbeidsforholdClient, organisasjonService)
    }

    @Bean
    open fun arbeidsforholdClient(): ArbeidsforholdClient {
        return ArbeidsforholdClientImpl(arbeidsforholdClient, baseurl, stsClient)
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

    @Bean
    open fun arbeidsforholdSystemdata(
        arbeidsforholdService: ArbeidsforholdService,
        textService: TextService
    ): ArbeidsforholdSystemdata {
        return ArbeidsforholdSystemdata(arbeidsforholdService, textService)
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
