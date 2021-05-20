package no.nav.sosialhjelp.soknad.web.oidc;

import com.nimbusds.jose.util.ResourceRetriever;
import no.nav.security.token.support.core.configuration.IssuerProperties;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;
import no.nav.security.token.support.test.FileResourceRetriever;
import no.nav.security.token.support.test.JwkGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;

@Configuration
public class OidcConfig {

    @Value("${oidc.issuer.selvbetjening.discoveryurl}")
    private String discoveryUrl;

    @Value("${oidc.issuer.selvbetjening.cookie_name}")
    private String cookieName;

    @Value("${oidc.issuer.selvbetjening.accepted_audience}")
    private String acceptedAudience;

    /**
     * Overskriver måten å hente ut OIDC-metadata. Istedenfor å hente det fra en internettadresse henter man det fra filsystemet.
     * Filene metadata.json og jwkset.json ligger i jar-pakken til maven-biblioteket token-validation-test-support.
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

    @Primary
    @Bean
    public MultiIssuerConfiguration testMultiIssuerConfiguration(ResourceRetriever resourceRetriever) {
        return new MultiIssuerConfiguration(getIssuerPropertiesMap(), resourceRetriever);
    }

    @Bean
    JwkGenerator jwkGenerator() {
        return new JwkGenerator();
    }

    public static boolean isOidcMock() {
        return "true".equalsIgnoreCase(System.getProperty("tillatmock")) &&
                "true".equalsIgnoreCase(System.getProperty("start.oidc.withmock"));
    }

    private Map<String, IssuerProperties> getIssuerPropertiesMap() {
        Map<String, IssuerProperties> issuerPropertiesMap = new HashMap<>();

        URL url = null;
        try {
            url = new URL(discoveryUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        var issuerProperties = new IssuerProperties(url, Collections.singletonList(acceptedAudience), cookieName);
        issuerPropertiesMap.put(SELVBETJENING, issuerProperties);
        return issuerPropertiesMap;
    }
}
