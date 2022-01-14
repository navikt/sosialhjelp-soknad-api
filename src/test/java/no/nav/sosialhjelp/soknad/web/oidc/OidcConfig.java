package no.nav.sosialhjelp.soknad.web.oidc;

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OidcConfig {

    @Value("${tillatmock}")
    private boolean tillatmock;

    @Value("${start.oidc.withmock}")
    private boolean startOidcWithMock;

    /**
     * Overskriver måten å hente ut OIDC-metadata. Istedenfor å hente det fra en internettadresse henter man det fra filsystemet.
     * Filene metadata.json og jwkset.json ligger i jar-pakken til maven-biblioteket token-validation-test-support.
     * metadata-tokenx.json ligger lokalt i test-resources.
     */
    @Primary
    @Bean
    public FileResourceRetriever fileResourceRetriever() {
        return new FileResourceRetriever("/metadata.json", "/metadata-tokenx.json", "/jwkset.json");
    }

    /** Overskriver filteret for å validere token */
    @Primary
    @Bean
    JaxrsJwtTokenValidationFilter jaxrsJwtTokenValidationFilter(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") MultiIssuerConfiguration multiIssuerConfiguration) {
        if (tillatmock && startOidcWithMock) {
            return new FakeOidcTokenValidatorFilter(multiIssuerConfiguration);
        } else {
            return new JaxrsJwtTokenValidationFilter(multiIssuerConfiguration);
        }
    }

    @Bean
    JwkGenerator jwkGenerator() {
        return new JwkGenerator();
    }

}
