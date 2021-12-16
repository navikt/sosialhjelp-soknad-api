package no.nav.sosialhjelp.soknad.tekster

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TextConfig {

    @Bean
    open fun navMessageSource(): NavMessageSource {
        return NavMessageSource()
    }

    @Bean
    open fun textService(navMessageSource: NavMessageSource): TextService {
        return TextService(navMessageSource)
    }
}
