package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy
import no.nav.sosialhjelp.soknad.client.maskinporten.MaskinportenClient
import no.nav.sosialhjelp.soknad.organisasjon.OrganisasjonService
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.web.selftest.Pingable
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class SkattbarInntektConfig(
    private val proxiedWebClientBuilder: WebClient.Builder,
    @Value("\${skatteetaten_api_baseurl}") private val baseurl: String,
    private val maskinportenClient: MaskinportenClient
) {

    @Bean
    open fun skattbarInntektService(skatteetatenClient: SkatteetatenClient): SkattbarInntektService {
        return SkattbarInntektService(skatteetatenClient)
    }

    @Bean
    open fun skatteetatenClient(): SkatteetatenClient {
        val skatteetatenClient = SkatteetatenClientImpl(skatteetatenWebClient, maskinportenClient)
        return createTimerProxy("SkatteetatenApi", skatteetatenClient, SkatteetatenClient::class.java)
    }

    @Bean
    open fun skatteetatenPing(skatteetatenClient: SkatteetatenClient): Pingable {
        return Pingable {
            val metadata = PingMetadata(baseurl, "SkatteetatenApi", false)
            try {
                skatteetatenClient.ping()
                Ping.lyktes(metadata)
            } catch (e: Exception) {
                Ping.feilet(metadata, e)
            }
        }
    }

    @Bean
    open fun skatteetatenSystemdata(
        skattbarInntektService: SkattbarInntektService,
        organisasjonService: OrganisasjonService,
        textService: TextService
    ): SkatteetatenSystemdata {
        return SkatteetatenSystemdata(skattbarInntektService, organisasjonService, textService)
    }

    private val skatteetatenWebClient: WebClient
        get() = proxiedWebClientBuilder
            .baseUrl(baseurl)
            .build()
}
