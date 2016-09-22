package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;

@Configuration
public class DkifWSConfig {

    public static final String DKIF_KEY = "start.dkif.withmock";

    @Value("${dkif.webservice.url}")
    private String dkifEndpoint;

    @Bean
    public DigitalKontaktinformasjonV1 dkifService() {
        DigitalKontaktinformasjonV1 mock = new DkifMock().dkifMock();
        DigitalKontaktinformasjonV1 prod = factory().withMDC().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Dkif", prod, mock, DKIF_KEY, DigitalKontaktinformasjonV1.class);
    }

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
                .withWsdl("classpath:dkif/no/nav/tjeneste/virksomhet/digitalKontaktinformasjon/v1/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/digitalKontaktinformasjon/v1/Binding", "DigitalKontaktinformasjon_v1"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/digitalKontaktinformasjon/v1/Binding", "DigitalKontaktinformasjon_v1Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }
}
