package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.common.rest.RestUtils;
import no.nav.sosialhjelp.soknad.consumer.svarut.SvarUtConsumer;
import no.nav.sosialhjelp.soknad.consumer.svarut.SvarUtConsumerImpl;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

@Configuration
public class SvarUtRestConfig {

    private static final int SVARUT_TIMEOUT = 16 * 60 * 1000;

    @Value("${svarut_url}")
    private String svarutUrl;

    @Value("${fiks_svarut_username}")
    private String svarutUsername;

    @Value("${fiks_svarut_password}")
    private String svarutPassword;

    @Bean
    public SvarUtConsumer svarUtConsumer() {
        var svarUt = new SvarUtConsumerImpl(svarutUrl, svarUtClient());
        return createTimerProxy("SvarUt", svarUt, SvarUtConsumer.class);
    }

    @Bean
    public Pingable svarUtPing(SvarUtConsumer svarUtConsumer) {
        return () -> {
            Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(svarutUrl, "SvarUt", false);
            try {
                svarUtConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }

    private Client svarUtClient() {
        var restConfig = RestUtils.RestConfig.builder()
                .connectTimeout(SVARUT_TIMEOUT)
                .readTimeout(SVARUT_TIMEOUT)
                .build();
        return RestUtils.createClient(restConfig)
                .register(MultiPartFeature.class)
                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().put(AUTHORIZATION.toString(), singletonList(getBasicAuthentication())));
    }

    private String getBasicAuthentication() {
        if (svarutUsername == null || svarutPassword == null) {
            throw new RuntimeException("svarutUsername eller svarutPassword er ikke tilgjengelig.");
        }
        var token = svarutUsername + ":" + svarutPassword;
        return "Basic " + DatatypeConverter.printBase64Binary(token.getBytes(StandardCharsets.UTF_8));
    }
}
