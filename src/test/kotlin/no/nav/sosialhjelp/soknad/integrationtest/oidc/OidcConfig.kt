package no.nav.sosialhjelp.soknad.integrationtest.oidc

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
open class OidcConfig(
    @Value("\${tillatmock}") private val tillatmock: Boolean = false,
    @Value("\${start.oidc.withmock}") private val startOidcWithMock: Boolean = false
) {

    /**
     * Overskriver måten å hente ut OIDC-metadata. Istedenfor å hente det fra en internettadresse henter man det fra filsystemet.
     * Filene metadata.json og jwkset.json ligger i jar-pakken til maven-biblioteket token-validation-test-support.
     * metadata-tokenx.json ligger lokalt i test-resources.
     */
    @Primary
    @Bean
    open fun fileResourceRetriever(): FileResourceRetriever {
        return FileResourceRetriever("/metadata.json", "/metadata-tokenx.json", "/jwkset.json")
    }

    /** Overskriver filteret for å validere token  */
    @Primary
    @Bean
    open fun jaxrsJwtTokenValidationFilter(multiIssuerConfiguration: MultiIssuerConfiguration?): JaxrsJwtTokenValidationFilter {
        return if (tillatmock && startOidcWithMock) {
            FakeOidcTokenValidatorFilter(multiIssuerConfiguration)
        } else {
            JaxrsJwtTokenValidationFilter(multiIssuerConfiguration)
        }
    }
}
