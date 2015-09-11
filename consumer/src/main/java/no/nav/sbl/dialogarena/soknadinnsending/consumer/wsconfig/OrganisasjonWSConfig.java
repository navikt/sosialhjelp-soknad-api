package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid.ArbeidsforholdMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;

@Configuration
public class OrganisasjonWSConfig {

    public static final String ARBEID_KEY = "start.arbeid.withmock";

    @Value("${soknad.webservice.arbeid.organisasjon.url}")
    private String organisasjonEndpoint;

    private ServiceBuilder<OrganisasjonV4>.PortTypeBuilder<OrganisasjonV4> factory() {
        return new ServiceBuilder<>(OrganisasjonV4.class)
                .asStandardService()
                .withAddress(organisasjonEndpoint)
                .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/organisasjon/v4/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding", "Organisasjon_v4"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding", "Organisasjon_v4Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public OrganisasjonV4 organisasjonEndpoint() {
        OrganisasjonV4 mock = new ArbeidsforholdMock().organisasjonMock();
        OrganisasjonV4 prod = factory().withSystemSecurity().get();
        return createSwitcher(prod, mock, ARBEID_KEY, OrganisasjonV4.class);
    }

    @Bean
    public OrganisasjonV4 organisasjonSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    Pingable organisasjonPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    organisasjonEndpoint().ping();
                    return Ping.lyktes("Organisasjon_v4");
                } catch (Exception e) {
                    return Ping.feilet("Organisasjon_v4", e);
                }
            }
        };
    }
}
