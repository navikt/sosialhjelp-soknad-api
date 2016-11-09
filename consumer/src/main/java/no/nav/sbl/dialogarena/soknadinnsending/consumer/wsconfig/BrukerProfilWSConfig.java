package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil.BrukerprofilMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;

@Configuration
public class BrukerProfilWSConfig {

    public static final String BRUKERPROFIL_KEY = "start.brukerprofil.withmock";

    @Value("${soknad.webservice.brukerprofil.brukerprofilservice.url}")
    private String brukerProfilEndpoint;

    private ServiceBuilder<BrukerprofilPortType>.PortTypeBuilder<BrukerprofilPortType> factory() {
        return new ServiceBuilder<>(BrukerprofilPortType.class)
                .asStandardService()
                .withAddress(brukerProfilEndpoint)
                .withWsdl("classpath:brukerprofil/no/nav/tjeneste/virksomhet/brukerprofil/v1/Brukerprofil.wsdl")
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public BrukerprofilPortType brukerProfilEndpoint() {
        BrukerprofilPortType mock = BrukerprofilMock.getInstance().getBrukerprofilPortTypeMock();
        BrukerprofilPortType prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Brukerprofil", prod, mock, BRUKERPROFIL_KEY, BrukerprofilPortType.class);
    }

    @Bean
    public BrukerprofilPortType brukerProfilSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable brukerprofilPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    brukerProfilSelftestEndpoint().ping();
                    return Ping.lyktes("Brukerprofil");
                } catch (Exception e) {
                    return Ping.feilet("Brukerprofil", e);
                }
            }
        };
    }
}
