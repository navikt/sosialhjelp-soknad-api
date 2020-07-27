package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class PersonWSConfig {

    private static final Logger logger = LoggerFactory.getLogger(PersonWSConfig.class);

    public static final String PERSON_KEY = "start.person.withmock";

    @Value("${soknad.webservice.person.personservice.url}")
    private String personEndpoint;

    /*private ServiceBuilder<PersonPortType>.PortTypeBuilder<PersonPortType> factory() {
        return new ServiceBuilder<>(PersonPortType.class)
                .asStandardService()
                .withAddress(personEndpoint)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/person/v1/Person.wsdl")
                .build()
                .withHttpsMock()
                .withMDC();
    }*/

    @Bean
    public PersonPortType personClient() {
        logger.info("Using personV1 endpoint: " + personEndpoint);
        if (MockUtils.isTillatMockRessurs()) {
            return new PersonMock().personMock();
        }
        PersonPortType prod = new CXFClient<>(PersonPortType.class)
                .address(personEndpoint)
                .configureStsForSubject()
                .build();
        return createTimerProxyForWebService("Person", prod, PersonPortType.class);
        /*
        PersonPortType mock = new PersonMock().personMock();
        PersonPortType prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Person", prod, mock, PERSON_KEY, PersonPortType.class);
         */
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
