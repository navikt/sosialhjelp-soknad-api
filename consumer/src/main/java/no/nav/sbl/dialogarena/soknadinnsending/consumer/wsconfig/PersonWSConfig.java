package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class PersonWSConfig {

    private static final int DEFAULT_RECEIVE_TIMEOUT = 20000;
    private static final int DEFUALT_CONNECTION_TIMEOUT = 20000;

    @Value("${soknad.webservice.person.personservice.url}")
    private String personEndpoint;

    @Bean
    public PersonPortType personClient() {
        if (MockUtils.isTillatMockRessurs()) {
            return new PersonMock().personMock();
        }

        PersonPortType prod = new CXFClient<>(PersonPortType.class)
                .wsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/person/v1/Person.wsdl")
                .timeout(DEFUALT_CONNECTION_TIMEOUT, DEFAULT_RECEIVE_TIMEOUT)
                .address(personEndpoint)
                .configureStsForSubject()
                .build();

        return createTimerProxyForWebService("Person", prod, PersonPortType.class);
    }

    public PersonPortType personSelftestEndpoint() {
        return new CXFClient<>(PersonPortType.class).address(personEndpoint).configureStsForSystemUser().build();
    }

    @Bean
    public Pingable personPingable() {
        return () -> {
            PingMetadata metadata = new PingMetadata(personEndpoint,"TPS - Person", true);
            try {
                personSelftestEndpoint().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
