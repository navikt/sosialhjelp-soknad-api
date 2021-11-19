package no.nav.sosialhjelp.soknad.adressesok

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AdressesokConfig(
    private val adressesokClient: AdressesokClient,
    private val kodeverkService: KodeverkService
) {

    @Bean
    open fun adressesokService(): AdressesokService {
        return AdressesokService(adressesokClient, kodeverkService)
    }
}
