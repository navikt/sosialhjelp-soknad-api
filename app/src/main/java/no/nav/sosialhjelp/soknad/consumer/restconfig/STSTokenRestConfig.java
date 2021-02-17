package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.sts.apigw.STSConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;
import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_NAV_APIKEY;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

@Configuration
public class STSTokenRestConfig {

    private static final String SOSIALHJELP_SOKNAD_API_STSTOKEN_APIKEY_PASSWORD = "SOSIALHJELP_SOKNAD_API_STSTOKEN_APIKEY_PASSWORD";
    private static final String SRVSOKNADSOSIALHJELP_SERVER_USERNAME = "SRVSOKNADSOSIALHJELP_SERVER_USERNAME";
    private static final String SRVSOKNADSOSIALHJELP_SERVER_PASSWORD = "SRVSOKNADSOSIALHJELP_SERVER_PASSWORD";

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
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().putSingle(HEADER_NAV_APIKEY, System.getenv(SOSIALHJELP_SOKNAD_API_STSTOKEN_APIKEY_PASSWORD)));
    }

    private String getBasicAuthentication() {
        String username = System.getenv(SRVSOKNADSOSIALHJELP_SERVER_USERNAME);
        String password = System.getenv(SRVSOKNADSOSIALHJELP_SERVER_PASSWORD);
        if (username == null || password == null) {
            throw new RuntimeException("Username eller password er ikke tilgjengelig.");
        }
        String token = username + ":" + password;
        return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes(StandardCharsets.UTF_8));
    }
}
