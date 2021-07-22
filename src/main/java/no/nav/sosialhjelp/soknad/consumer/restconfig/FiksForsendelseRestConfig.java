package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.ks.fiks.svarut.klient.SvarUtKlientApi;
import no.ks.fiks.svarut.klient.SvarUtKlientApiImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sosialhjelp.metrics.MetricsFactory.createTimerProxyForWebService;

@Configuration
public class FiksForsendelseRestConfig {

    @Value("${svarut_url}")
    private String svarutUrl;

    private String avsender = "";
    private String servicePassord = "";

    @Bean
    public SvarUtKlientApi svarUtKlientApi() {
        var svarUt = new SvarUtKlientApiImpl(svarutUrl, avsender, servicePassord);
        return createTimerProxyForWebService("SvarUt", svarUt, SvarUtKlientApi.class);
    }
}
