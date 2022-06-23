package no.nav.sosialhjelp.soknad.client.unleash

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.repository.HttpToggleFetcher
import no.finn.unleash.repository.ToggleFetcher
import no.finn.unleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UnleashConfig(
    @Value("\${unleash_api_url}") private val baseurl: String,
    @Value("\${unleash_instance_id:prod-sbs}") private val instanceId: String
) {

    @Bean
    open fun unleashClient(): Unleash {
        return DefaultUnleash(config, ByInstanceIdStrategy(instanceId))
    }

    @Bean
    open fun unleashToggleFetcher(): ToggleFetcher? {
        return HttpToggleFetcher(config)
    }

    private val config: UnleashConfig =
        UnleashConfig.builder()
            .appName("sosialhjelp-soknad-api")
            .instanceId(instanceId)
            .unleashAPI(baseurl)
            .build()
}
