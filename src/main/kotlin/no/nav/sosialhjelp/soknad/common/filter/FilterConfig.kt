package no.nav.sosialhjelp.soknad.common.filter

import no.nav.sosialhjelp.soknad.common.oidc.OidcTokenValidatorFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.filter.CharacterEncodingFilter

@Configuration
open class FilterConfig {

    @Bean
    open fun characterEncodingFilter(): CharacterEncodingFilter {
        return CharacterEncodingFilter("UTF-8", true)
    }

    @Bean
    @Profile("!(mock-alt | test)")
    open fun oidcTokenValidatorFilter(): OidcTokenValidatorFilter {
        return OidcTokenValidatorFilter()
    }
}
