package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte;

import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
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

    @Bean
    Bostotte getBostotteImpl() {
        if(MockUtils.isTillatMockRessurs()) {
            return new MockBostotteImpl(this);
        }
        return new BostotteImpl(this, new RestTemplate());
    }

    public String getUri() {
        return uri;
    }
}
