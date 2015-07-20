package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.kodeverk.KodeverkMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;

@Configuration
public class KodeverkWSConfig {

    public static final String KODEVERK_KEY = "start.kodeverk.withmock";

    @Value("${sendsoknad.webservice.kodeverk.url}")
    private String kodeverkEndPoint;

    private ServiceBuilder<KodeverkPortType>.PortTypeBuilder<KodeverkPortType> factory() {
        return new ServiceBuilder<>(KodeverkPortType.class)
                .asStandardService()
                .withAddress(kodeverkEndPoint)
                .withWsdl("classpath:kodeverk/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl")
                .build()
                .withHttpsMock();
    }

    @Bean
    public KodeverkPortType kodeverkEndpoint() {
        KodeverkPortType prod = factory().withSystemSecurity().get();
        KodeverkPortType mock = new KodeverkMock().kodeverkMock();
        return createSwitcher(prod, mock, KODEVERK_KEY, KodeverkPortType.class);
    }

    @Bean
    public KodeverkPortType kodeverkSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable kodeverkPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    kodeverkSelftestEndpoint().ping();
                    return Ping.lyktes("Kodeverk_v2");
                } catch (Exception e) {
                    return Ping.feilet("Kodeverk_v2", e);
                }
            }
        };
    }
}
