package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.URL;

@Configuration
public class DkifWSConfig {

    @Value("${dkif.webservice.url}")
    private URL dkifEndpoint;

    private static final String DKIF_WSDL_URL = "classpath:dkif/dkif/no/nav/tjeneste/virksomhet/digitalKontaktinformasjon/v1/DigitalKontaktinformasjon.wsdl";

    @Bean
    public DigitalKontaktinformasjonV1 dkifService() {
        return factory().withMDC().withUserSecurity().get();
    }

    @Bean
    public DigitalKontaktinformasjonV1 dkifServiceSelftest() {
        return factory().withSystemSecurity().get();
    }

    private ServiceBuilder<DigitalKontaktinformasjonV1>.PortTypeBuilder<DigitalKontaktinformasjonV1> factory() {
        return new ServiceBuilder<>(DigitalKontaktinformasjonV1.class)
                .asStandardService()
                .withAddress(dkifEndpoint.toString())
                .withWsdl(DKIF_WSDL_URL)
                .build()
                .withHttpsMock()
                .withMDC();
    }
}
