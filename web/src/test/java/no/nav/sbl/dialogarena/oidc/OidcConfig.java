package no.nav.sbl.dialogarena.oidc;

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;
import no.nav.security.token.support.test.FileResourceRetriever;
import no.nav.security.token.support.test.JwkGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OidcConfig {

    /**
     * Overskriver måten å hente ut OIDC-metadata. Istedenfor å hente det fra en internettadresse henter man det fra filsystemet.
     * Filene metadata.json og jwkset.json ligger i jar-pakken til maven-biblioteket oidc-test-support.
     */
    @Primary
    @Bean
    public FileResourceRetriever fileResourceRetriever() {
        return new FileResourceRetriever("/metadata.json",  "/jwkset.json");
    }

    /** Overskriver filteret for å validere token */
    @Primary
    @Bean
    JaxrsJwtTokenValidationFilter FakeOidcTokenValidatorFilter(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") MultiIssuerConfiguration multiIssuerConfiguration) {
        if (isOidcMock()) {
            return new FakeOidcTokenValidatorFilter(multiIssuerConfiguration);
        } else {
            return new JaxrsJwtTokenValidationFilter(multiIssuerConfiguration);
        }
    }

    @Bean
    JwkGenerator jwkGenerator() {
        return new JwkGenerator();
    }

    public static boolean isOidcMock() {
        return "true".equalsIgnoreCase(System.getProperty("tillatmock")) &&
                "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock"));
    }

}
