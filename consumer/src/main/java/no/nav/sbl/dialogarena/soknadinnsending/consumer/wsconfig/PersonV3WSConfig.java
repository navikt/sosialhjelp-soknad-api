package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.sendsoknad.domain.mock.MockUtils;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonV3Mock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import java.util.UUID;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class PersonV3WSConfig {

    @Value("${soknad.webservice.personv3.personservice.url}")
    private String personv3EndpointUrl;

    @Bean
    public PersonV3 personV3Endpoint() {
        if (MockUtils.isTillatMockRessurs()) {
            return new PersonV3Mock().personV3Mock();
        }
        PersonV3 client = new CXFClient<>(PersonV3.class)
                .address(personv3EndpointUrl)
                .configureStsForSubject()
                .build();
        return createTimerProxyForWebService("Person", client, PersonV3.class);
    }

    @Bean
    public Pingable personv3Pingable() {
        return () -> {
            PingMetadata metadata = new PingMetadata(UUID.randomUUID().toString(), personv3EndpointUrl,"TPS - Person", true);
            try {
                new CXFClient<>(PersonV3.class)
                        .address(personv3EndpointUrl)
                        .configureStsForSystemUser()
                        .build()
                        .ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
