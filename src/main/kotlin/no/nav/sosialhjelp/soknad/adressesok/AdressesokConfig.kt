package no.nav.sosialhjelp.soknad.adressesok

import no.nav.sosialhjelp.soknad.client.azure.AzureadService
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.common.rest.RestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.ws.rs.client.Client

@Configuration
open class AdressesokConfig(
    @Value("\${pdl_api_url}") private val baseurl: String,
    @Value("\${pdl_api_scope}") private val pdlScope: String,
    private val azureadService: AzureadService,
    private val kodeverkService: KodeverkService,
) {

    @Bean
    open fun adressesokService(adressesokClient: AdressesokClient): AdressesokService {
        return AdressesokService(adressesokClient, kodeverkService)
    }

    @Bean
    open fun adressesokClient(): AdressesokClient {
        return AdressesokClient(client, baseurl, azureadService, pdlScope)
    }

    private val client: Client
        get() = RestUtils.createClient()
}
