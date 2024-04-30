package no.nav.sosialhjelp.soknad.v2.config

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class MockUnleashConfig {
    //    @MockkBean
//    private lateinit var unleashClient: Unleash

//    @MockkBean
//    private lateinit var unleashToggleFetcher: MutableList<String>

    @Bean
    fun unleashClient(): Unleash {
        return FakeUnleash()
    }

    @Bean
    fun unleashToggleFetcher(): MutableList<String> {
        return unleashClient().more().featureToggleNames
    }
}
