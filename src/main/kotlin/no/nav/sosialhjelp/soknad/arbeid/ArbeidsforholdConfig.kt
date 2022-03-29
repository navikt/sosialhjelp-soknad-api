package no.nav.sosialhjelp.soknad.arbeid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.client.tokenx.TokendingsService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class ArbeidsforholdConfig(
    @Value("\${aareg_proxy_url}") private val aaregProxyUrl: String,
    @Value("\${fss_proxy_audience}") private val fssProxyAudience: String,
    private val tokendingsService: TokendingsService
) {

    @Bean
    open fun arbeidsforholdClient(): ArbeidsforholdClient {
        return ArbeidsforholdClient(arbeidsforholdClient, aaregProxyUrl, fssProxyAudience, tokendingsService)
    }

    private val arbeidsforholdClient: Client
        get() = RestUtils.createClient()
            .register(arbeidsforholdMapper)

    private val arbeidsforholdMapper: ObjectMapper
        get() = jacksonObjectMapper()
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .registerModule(JavaTimeModule())
}
