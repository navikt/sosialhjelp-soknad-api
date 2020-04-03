package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonV3Mock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class PersonV3WSConfig {

    public static final String PERSON_KEY = "start.person.withmock";

    @Value("${soknad.webservice.personv3.personservice.url}")
    private String personv3EndpointUrl;

    private ServiceBuilder<PersonV3>.PortTypeBuilder<PersonV3> factory() {
        return new ServiceBuilder<>(PersonV3.class)
                .asStandardService()
                .withAddress(personv3EndpointUrl)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/person/v3/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/person/v3/Binding", "Person_v3"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/person/v3/Binding", "Person_v3Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public PersonV3 personV3Endpoint() {
        PersonV3 mock = new PersonV3Mock().personV3Mock();
        PersonV3 prod = factory().withUserSecurity().get();
        return createMetricsProxyWithInstanceSwitcher("Person", prod, mock, PERSON_KEY, PersonV3.class);
    }

    @Bean
    public PersonV3 personV3SelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    public Pingable personv3Pingable() {
        return () -> {
            PingMetadata metadata = new PingMetadata(personv3EndpointUrl,"TPS - Person", true);
            try {
                personV3SelftestEndpoint().ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
