package no.nav.sosialhjelp.soknad.config

import io.mockk.mockk
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SoknadinnsendingLocalConfig {

    @Bean
    open fun tilgangskontroll(): Tilgangskontroll {
        return mockk(relaxed = true)
    }
}
