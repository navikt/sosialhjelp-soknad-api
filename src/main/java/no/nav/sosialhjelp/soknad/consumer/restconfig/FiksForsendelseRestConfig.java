package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.ks.fiks.svarut.klient.SvarUtKlientApi;
import no.ks.fiks.svarut.klient.SvarUtKlientApiImpl;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxy;

@Configuration
public class FiksForsendelseRestConfig {

    @Value("${svarut_url}")
    private String svarutUrl;

    @Value("${fiks_svarut_username}")
    private String svarutUsername;

    @Value("${fiks_svarut_password}")
    private String svarutPassword;

    @Bean
    public SvarUtKlientApi svarUtKlientApi() {
        var httpClient = httpClient();
        var svarUt = new SvarUtKlientApiImpl(svarutUrl, httpClient, svarutUsername, svarutPassword);
        return createTimerProxy("SvarUt", svarUt, SvarUtKlientApi.class);
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
}
