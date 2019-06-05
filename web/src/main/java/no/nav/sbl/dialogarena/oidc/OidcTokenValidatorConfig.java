package no.nav.sbl.dialogarena.oidc;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.security.oidc.configuration.IssuerProperties;
import no.nav.security.oidc.configuration.MultiIssuerConfiguration;
import no.nav.security.oidc.configuration.OIDCResourceRetriever;
import no.nav.security.oidc.jaxrs.servlet.JaxrsOIDCTokenValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Configuration
public class OidcTokenValidatorConfig {
    private static final Logger log = LoggerFactory.getLogger(OidcTokenValidatorConfig.class);

    @Bean
    public JaxrsOIDCTokenValidationFilter jaxrsOIDCTokenValidationFilter(MultiIssuerConfiguration multiIssuerConfiguration) {
        return new JaxrsOIDCTokenValidationFilter(multiIssuerConfiguration);
    }

    @Bean
    public MultiIssuerConfiguration MultiIssuerConfiguration(OIDCResourceRetriever resourceRetriever) {
        if (MockUtils.isTillatMockRessurs()) {
            return new MultiIssuerConfiguration(new HashMap<>(), resourceRetriever);
        }
        return new MultiIssuerConfiguration(getIssuerPropertiesMap(), resourceRetriever);
    }

    /** Is overridden in test scope **/
    @Bean
    public OIDCResourceRetriever oidcResourceRetriever() {
        return new OIDCResourceRetriever();
    }

    private Map<String, IssuerProperties> getIssuerPropertiesMap() {
        Map<String, IssuerProperties> issuerPropertiesMap = new HashMap<>();
        String[] issuers = {"selvbetjening"};
        Arrays.stream(issuers)
                .forEach(i -> addIssuerToMap(i, issuerPropertiesMap));
        return issuerPropertiesMap;
    }

    private void addIssuerToMap(String issuer, Map<String, IssuerProperties> issuerPropertiesMap ) {
        IssuerProperties issuerProperties = new IssuerProperties(getDiscoveryUrl(issuer), getAcceptedAudiences(issuer), getCookieName(issuer));
        issuerProperties.setProxyUrl(getProxyUrl(issuer));
        issuerPropertiesMap.put(issuer, issuerProperties);
    }

    private String getCookieName(String issuer) {
        return System.getProperty("oidc.issuer." + issuer + ".cookie_name");
    }

    private List<String> getAcceptedAudiences(String issuer) {
        List<String> acceptedAudiences = new ArrayList<>();
        acceptedAudiences.add(System.getProperty("oidc.issuer." + issuer + ".accepted_audience"));
        return acceptedAudiences;
    }

    private URL getDiscoveryUrl(String issuer) {
        try {
            return new URL(System.getProperty("oidc.issuer." + issuer + ".discoveryurl"));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Kunne ikke parse property 'oidc.issuer."+ issuer + ".discoveryurl' til en URL", e);
        }
    }

    private URL getProxyUrl(String issuer) {
        try {
            return new URL(System.getProperty("oidc.issuer." + issuer + ".proxy_url"));
        } catch (MalformedURLException e) {
            log.info("Kunne ikke parse property 'oidc.issuer."+ issuer + ".proxy_url' til en URL. Fortsetter uten proxy." );
            return null;
        }
    }
}
