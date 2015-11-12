package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DkifWSConfig {

    @Value("${dkif.webservice.url}")
    private String dkifEndpoint;

    private static final String DKIF_WSDL= "classpath:dkif/dkif/no/nav/tjeneste/virksomhet/digitalKontaktinformasjon/v1/DigitalKontaktinformasjon.wsdl";

    @Bean
    public DigitalKontaktinformasjonV1 dkifService() {
        return factory().withMDC().withUserSecurity().get();
    }

    @Bean
    public DigitalKontaktinformasjonV1 dkifServiceSelftest() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    public Pingable dkifPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    dkifServiceSelftest().ping();
                    return Ping.lyktes("Dkif");
                } catch (Exception ex) {
                    return Ping.feilet("Dkif", ex);
                }
            }
        };
    }

    private ServiceBuilder<DigitalKontaktinformasjonV1>.PortTypeBuilder<DigitalKontaktinformasjonV1> factory() {
        return new ServiceBuilder<>(DigitalKontaktinformasjonV1.class)
                .asStandardService()
                .withAddress(dkifEndpoint)
                .withWsdl("classpath:dkif/dkif/no/nav/tjeneste/virksomhet/digitalKontaktinformasjon/v1/DigitalKontaktinformasjon.wsdl")
                .build()
                .withHttpsMock()
                .withMDC();
    }
}
