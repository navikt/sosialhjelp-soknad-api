package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.ks.fiks.svarut.klient.SvarUtKlientApi;
import no.ks.fiks.svarut.klient.SvarUtKlientApiImpl;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import javax.ws.rs.client.Client;
//import javax.ws.rs.client.ClientRequestFilter;
//import javax.xml.bind.DatatypeConverter;
//import java.nio.charset.StandardCharsets;
//
//import static java.util.Collections.singletonList;
//import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy;
//import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

@Configuration
public class SvarUtRestConfig {

    private static final int SVARUT_TIMEOUT = 16 * 60 * 1000;

    @Value("${svarut_url}")
    private String svarutUrl;

    @Value("${fiks_svarut_username}")
    private String svarutUsername;

    @Value("${fiks_svarut_password}")
    private String svarutPassword;

//    @Bean
//    public SvarUtConsumer svarUtConsumer() {
//        var svarUt = new SvarUtConsumerImpl(svarutUrl, svarUtClient());
//        return createTimerProxy("SvarUt", svarUt, SvarUtConsumer.class);
//    }

    @Bean
    public SvarUtKlientApi svarUtKlientApi() {
        return new SvarUtKlientApiImpl(svarutUrl, httpClient(), svarutUsername, svarutPassword);
    }

    private HttpClient httpClient() {
        var sslContextFactory = new SslContextFactory.Client();
        var httpClient = new HttpClient(sslContextFactory);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return httpClient;
    }

//    private Client svarUtClient() {
//        var restConfig = RestUtils.RestConfig.builder()
//                .connectTimeout(SVARUT_TIMEOUT)
//                .readTimeout(SVARUT_TIMEOUT)
//                .build();
//        return RestUtils.createClient(restConfig)
//                .register(MultiPartFeature.class)
//                .register((ClientRequestFilter) requestContext -> requestContext.getHeaders().put(AUTHORIZATION.toString(), singletonList(getBasicAuthentication())));
//    }
//
//    private String getBasicAuthentication() {
//        if (svarutUsername == null || svarutPassword == null) {
//            throw new RuntimeException("svarutUsername eller svarutPassword er ikke tilgjengelig.");
//        }
//        var token = svarutUsername + ":" + svarutPassword;
//        return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes(StandardCharsets.UTF_8));
//    }
}
