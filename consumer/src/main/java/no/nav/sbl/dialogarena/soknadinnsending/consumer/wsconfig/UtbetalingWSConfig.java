package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.utbetaling.UtbetalMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.utbetaling.v1.UtbetalingV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class UtbetalingWSConfig {

    public static final String UTBETALING_KEY = "start.utbetaling.withmock";

    @Value("${utbetaling.v1.url}")
    private String utbetalingEndpoint;

    /*private ServiceBuilder<UtbetalingV1>.PortTypeBuilder<UtbetalingV1> factory() {
        return new ServiceBuilder<>(UtbetalingV1.class)
                .asStandardService()
                .withAddress(utbetalingEndpoint)
                .withWsdl("classpath:/wsdl/utbetaling/no/nav/tjeneste/virksomhet/utbetaling/v1/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/utbetaling/v1/Binding", "Utbetaling_v1"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/utbetaling/v1/Binding", "Utbetaling_v1Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }*/

    @Bean
    public UtbetalingV1 utbetalingV1Client() {
        if (MockUtils.isTillatMockRessurs()) {
            return new UtbetalMock().utbetalMock();
        }
        UtbetalingV1 prod = new CXFClient<>(UtbetalingV1.class).address(utbetalingEndpoint).configureStsForSubject().build();
        return createTimerProxyForWebService("Utbetaling", prod, UtbetalingV1.class);
        /*UtbetalingV1 mock = new UtbetalMock().utbetalMock();
        UtbetalingV1 prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Utbetaling", prod, mock, UTBETALING_KEY, UtbetalingV1.class);*/
    }

    @Bean
    public Pingable utbetalingPing() {
        UtbetalingV1 selftestClient = new CXFClient<>(UtbetalingV1.class).address(utbetalingEndpoint).configureStsForSystemUser().build();

        return new Pingable() {
            Ping.PingMetadata metadata = new Ping.PingMetadata(utbetalingEndpoint,"Utbetaling_v1", false);

            @Override
            public Ping ping() {
                try {
                    selftestClient.ping();
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }

}
