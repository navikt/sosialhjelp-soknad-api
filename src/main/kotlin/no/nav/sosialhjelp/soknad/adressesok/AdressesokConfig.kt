package no.nav.sosialhjelp.soknad.adressesok

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.client.pdl.PdlConfig
import no.nav.sosialhjelp.soknad.client.sts.StsClient
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class AdressesokConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    private val kodeverkService: KodeverkService,
    private val stsClient: StsClient
) : PdlConfig(baseurl) {

    @Bean
    open fun adressesokService(adressesokClient: AdressesokClient): AdressesokService {
        return AdressesokService(adressesokClient, kodeverkService)
    }

    @Bean
    open fun adressesokClient(): AdressesokClient {
        return AdressesokClient(client, baseurl, stsClient)
    }

    private val client: Client
        get() = RestUtils.createClient().register(pdlApiKeyRequestFilter)
}
