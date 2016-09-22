package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonMock;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonPortTypeMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;

@Configuration
public class PersonWSConfig {

    public static final String PERSON_KEY = "start.person.withmock";
    private PersonMock personMock = PersonMock.getInstance();

    @Value("${soknad.webservice.person.personservice.url}")
    private String personEndpoint;

    private ServiceBuilder<PersonPortType>.PortTypeBuilder<PersonPortType> factory() {
        return new ServiceBuilder<>(PersonPortType.class)
                .asStandardService()
                .withAddress(personEndpoint)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/person/v1/Person.wsdl")
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public PersonPortType personEndpoint() {
        PersonPortTypeMock mock = personMock.getPersonPortTypeMock();
        PersonPortType prod = factory().withUserSecurity().get();
        return createSwitcher(prod, mock, PERSON_KEY, PersonPortType.class);
    }

    @Bean
    public PersonPortType personSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    public Pingable personPingable() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    personSelftestEndpoint().ping();
                    return Ping.lyktes("TPS_PERSON");
                } catch (Exception ex) {
                    return Ping.feilet("TPS_PERSON", ex);
                }
            }
        };
    }
}
