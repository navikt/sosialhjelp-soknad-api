package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.dkif.DkifMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjon_v1PortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;

@Configuration
public class DkifWSConfig {

    public static final String DKIF_KEY = "start.dkif.withmock";

    @Value("${dkif.webservice.url}")
    private String dkifEndpoint;

    @Bean
    public DigitalKontaktinformasjon_v1PortType dkifService() {
        DigitalKontaktinformasjon_v1PortType mock = new DkifMock().dkifMock();
        DigitalKontaktinformasjon_v1PortType prod = factory().withMDC().withUserSecurity().get();
        return createSwitcher(prod, mock, DKIF_KEY, DigitalKontaktinformasjon_v1PortType.class);
    }

    @Bean
    public DigitalKontaktinformasjon_v1PortType dkifServiceSelftest() {
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

    private ServiceBuilder<DigitalKontaktinformasjon_v1PortType>.PortTypeBuilder<DigitalKontaktinformasjon_v1PortType> factory() {
        return new ServiceBuilder<>(DigitalKontaktinformasjon_v1PortType.class)
                .asStandardService()
                .withAddress(dkifEndpoint)
                .build()
                .withHttpsMock()
                .withMDC();
    }
}
