package no.nav.sosialhjelp.soknad.v2.config

import com.ninjasquad.springmockk.MockkBean
import io.getunleash.DefaultUnleash
import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.sosialhjelp.soknad.app.featuretoggle.unleash.ByInstanceIdStrategy
import org.springframework.boot.test.context.TestConfiguration
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