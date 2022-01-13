//package no.nav.sosialhjelp.soknad.web.oidc;
//
//import no.nav.security.token.support.core.configuration.IssuerProperties;
//import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
//import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
//import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.TOKENX;
//
//@Configuration
//public class OidcTokenValidatorConfig {
//    private static final Logger log = LoggerFactory.getLogger(OidcTokenValidatorConfig.class);
//
//    @Value("${oidc.issuer.selvbetjening.cookie_name}")
//    private String cookieName;
//    @Value("${oidc.issuer.selvbetjening.accepted_audience}")
//    private String acceptedAudience;
//    @Value("${oidc.issuer.selvbetjening.discoveryurl}")
//    private String discoveryUrl;
//    @Value("${oidc.issuer.selvbetjening.proxy_url}")
//    private String proxyUrl;
//
//    @Value("${oidc.issuer.tokenx.accepted_audience}")
//    private String acceptedAudienceTokenx;
//    @Value("${oidc.issuer.tokenx.discoveryurl}")
//    private String discoveryUrlTokenx;
//
//    @Bean
//    public JaxrsJwtTokenValidationFilter jaxrsJwtTokenValidationFilter(MultiIssuerConfiguration multiIssuerConfiguration) {
//        return new JaxrsJwtTokenValidationFilter(multiIssuerConfiguration);
//    }
//
//    @Bean
//    public MultiIssuerConfiguration MultiIssuerConfiguration(ProxyAwareResourceRetriever resourceRetriever) {
//        return new MultiIssuerConfiguration(getIssuerPropertiesMap(), resourceRetriever);
//    }
//
//    /** Is overridden in test scope **/
//    @Bean
//    public ProxyAwareResourceRetriever proxyAwareResourceRetriever() {
//        return new ProxyAwareResourceRetriever();
//    }
//
//    private Map<String, IssuerProperties> getIssuerPropertiesMap() {
//        Map<String, IssuerProperties> issuerPropertiesMap = new HashMap<>();
//        issuerPropertiesMap.put(SELVBETJENING, selvbetjeningProperties());
//        issuerPropertiesMap.put(TOKENX, tokenxProperties());
//        return issuerPropertiesMap;
//    }
//
//    private IssuerProperties selvbetjeningProperties() {
//        var issuerProperties = new IssuerProperties(toUrl(discoveryUrl), Collections.singletonList(acceptedAudience), cookieName);
//        issuerProperties.setProxyUrl(proxyUrl(proxyUrl, SELVBETJENING));
//        return issuerProperties;
//    }
//
//    private IssuerProperties tokenxProperties() {
//        return new IssuerProperties(toUrl(discoveryUrlTokenx), Collections.singletonList(acceptedAudienceTokenx));
//    }
//
//    private URL toUrl(String url) {
//        try {
//            return new URL(url);
//        } catch (MalformedURLException e) {
//            throw new IllegalStateException("Kunne ikke parse property til en URL", e);
//        }
//    }
//
//    private URL proxyUrl(String proxyUrl, String issuer) {
//        try {
//            return new URL(proxyUrl);
//        } catch (MalformedURLException e) {
//            log.info("Kunne ikke parse property 'oidc.issuer.{}.proxy_url' til en URL. Fortsetter uten proxy.", issuer);
//            return null;
//        }
//    }
//}
