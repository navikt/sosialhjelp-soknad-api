package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackageClasses = Bostotte.class)
public class BostotteConfig {

    @Value("${soknad.bostotte.url}")
    private String uri = "";

    @Value("${soknad.bostotte.husbanken.app.key}")
    private String appKey = "appKey";

    @Value("${soknad.bostotte.husbanken.username}")
    private String username = "username";

    @Value("${soknad.bostotte.ping.url}")
    private String pingUrl;

    @Bean
    Bostotte getBostotteImpl() {
        if(MockUtils.isTillatMockRessurs()) {
            return new MockBostotteImpl();
        }
        return new BostotteImpl(this, new RestTemplate());
    }

    @Bean
    public Pingable forsendelsePing() {
        if(MockUtils.isTillatMockRessurs()) {
            return null;
        }
        return BostotteImpl.createHusbankenPing(this, new RestTemplate());
    }

    public String getUri() {
        return uri;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getUsername() {
        return username;
    }

    public String getPingUrl() {
        return pingUrl;
    }
}
