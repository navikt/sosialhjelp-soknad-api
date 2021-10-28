package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.sts.STSConsumer;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

@Configuration
public class STSTokenRestConfig {

    private static final String STSTOKEN_APIKEY = "STSTOKEN_APIKEY";

    @Value("${srvsoknadsosialhjelp.server.username}")
    private String srvsoknadsosialhjelpServerUsername;

    @Value("${srvsoknadsosialhjelp.server.password}")
    private String srvsoknadsosialhjelpServerPassword;

    @Value("${securitytokenservice_apigw_url}")
    private String endpoint;

    @Bean
    public STSConsumer stsConsumer() {
        return new STSConsumer(STSClient(), endpoint);
    }

    @Bean
    public Pingable stsTokenPing() {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(endpoint, "STSToken", false);
            try {
                stsConsumer().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client STSClient() {
        return RestUtils.createClient()
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().put(AUTHORIZATION.toString(), singletonList(getBasicAuthentication())))
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, System.getenv(STSTOKEN_APIKEY)));
    }

    private String getBasicAuthentication() {
        if (srvsoknadsosialhjelpServerUsername == null || srvsoknadsosialhjelpServerPassword == null) {
            throw new RuntimeException("Username eller password er ikke tilgjengelig.");
        }
        String token = srvsoknadsosialhjelpServerUsername + ":" + srvsoknadsosialhjelpServerPassword;
        return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes(StandardCharsets.UTF_8));
    }
}
