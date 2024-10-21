package no.nav.sosialhjelp.soknad.v2.config

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class MockUnleashConfig {
    @Bean
    fun unleashClient(): Unleash = FakeUnleash()

    @Bean
    fun unleashToggleFetcher(): MutableList<String> = FakeUnleash().more().featureToggleNames
}
