package no.nav.sbl.dialogarena.oidc;

import no.nav.security.oidc.configuration.MultiIssuerConfiguration;
import no.nav.security.oidc.configuration.OIDCResourceRetriever;
import no.nav.security.oidc.jaxrs.servlet.JaxrsOIDCTokenValidationFilter;
import no.nav.security.oidc.test.support.FileResourceRetriever;
import no.nav.security.oidc.test.support.JwkGenerator;
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
    public OIDCResourceRetriever oidcResourceRetriever() {
        return new FileResourceRetriever("/metadata.json",  "/jwkset.json");
    }

    /** Overskriver filteret for å validere token */
    @Primary
    @Bean
    JaxrsOIDCTokenValidationFilter FakeOidcTokenValidatorFilter(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") MultiIssuerConfiguration multiIssuerConfiguration) {
        if (isOidcMock()) {
            return new FakeOidcTokenValidatorFilter(multiIssuerConfiguration);
        } else {
            return new JaxrsOIDCTokenValidationFilter(multiIssuerConfiguration);
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
