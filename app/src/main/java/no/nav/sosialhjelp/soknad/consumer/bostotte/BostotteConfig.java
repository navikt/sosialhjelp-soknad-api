package no.nav.sosialhjelp.soknad.consumer.bostotte;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackageClasses = Bostotte.class)
public class BostotteConfig {

    @Value("${soknad.bostotte.url}")
    private String uri = "";

    @Value("${soknad.bostotte.ping.url}")
    private String pingUrl;

    @Bean
    Bostotte getBostotteImpl() {
        if (MockUtils.isTillatMockRessurs()) {
            return new MockBostotteImpl();
        }
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        return new BostotteImpl(this, restTemplate);
    }

    @Bean
    public Pingable opprettHusbankenPing() {
        if (MockUtils.isTillatMockRessurs()) {
            return null;
        }
        return BostotteImpl.opprettHusbankenPing(this, new RestTemplate(getClientHttpRequestFactory()));
    }

    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(10_000);
        clientHttpRequestFactory.setReadTimeout(10_000);
        return clientHttpRequestFactory;
    }

    public String getUri() {
        return uri;
    }

    public String getPingUrl() {
        return pingUrl;
    }
}
