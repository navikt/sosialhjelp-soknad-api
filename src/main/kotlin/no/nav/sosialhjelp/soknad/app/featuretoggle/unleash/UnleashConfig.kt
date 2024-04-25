package no.nav.sosialhjelp.soknad.app.featuretoggle.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class UnleashConfig(
    @Value("\${unleash_env}") private val environment: String,
    @Value("\${unleash_server_api_url}") private val baseurl: String,
    @Value("\${unleash_server_api_token}") private val apiToken: String,
) {
    @Bean
    fun unleashClient(): Unleash {
        val byInstanceIdStrategy = ByInstanceIdStrategy(environment)
        val config =
            UnleashConfig.builder()
                .appName("sosialhjelp-soknad-api")
                .environment(environment)
                .unleashAPI("$baseurl/api")
                .apiKey(apiToken)
                .build()

        return DefaultUnleash(
            config,
            byInstanceIdStrategy,
        )
    }

    @Bean
    fun unleashToggleFetcher(): MutableList<String> {
        return unleashClient().more().featureToggleNames
    }
}
