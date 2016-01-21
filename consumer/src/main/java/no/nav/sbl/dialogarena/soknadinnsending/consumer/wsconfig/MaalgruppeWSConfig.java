package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.MaalgrupperMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.MaalgruppeV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;

@Configuration
public class MaalgruppeWSConfig {

    public static final String MAALGRUPPE_KEY = "start.maalgruppe.withmock";
    @Value("${soknad.webservice.arena.maalgruppe.url}")
    private String maalgruppeEndpoint;

    @Bean
    public MaalgruppeV1 maalgruppeEndpoint() {
        MaalgruppeV1 mock = maalgruppeEndpointMock();
        MaalgruppeV1 prod = maalgruppeEndpointWS();
        return createSwitcher(prod, mock, MAALGRUPPE_KEY, MaalgruppeV1.class);
    }

    private ServiceBuilder<MaalgruppeV1>.PortTypeBuilder<MaalgruppeV1> factory() {
        return new ServiceBuilder<>(MaalgruppeV1.class)
                .asStandardService()
                .withAddress(maalgruppeEndpoint)
                .withWsdl("classpath:/no/nav/tjeneste/virksomhet/maalgruppe/v1/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/maalgruppe/v1/Binding", "maalgruppe_v1"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/maalgruppe/v1/Binding", "maalgruppe_v1Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public MaalgruppeV1 maalgruppeEndpointMock() {
        return MaalgrupperMock.maalgruppeV1();
    }

    @Bean
    public MaalgruppeV1 maalgruppeEndpointWS() {
        return factory().withUserSecurity().get();
    }

    @Bean
    public MaalgruppeV1 maalgruppeSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable maalgruppePing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    maalgruppeSelftestEndpoint().ping();
                    return Ping.lyktes("ArenaMålgruppe");
                } catch (Exception e) {
                    return Ping.feilet("ArenaMålgruppe", e);
                }
            }
        };
    }
}
