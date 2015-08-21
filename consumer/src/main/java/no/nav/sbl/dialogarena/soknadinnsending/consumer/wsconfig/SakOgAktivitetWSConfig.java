package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.AktiviteterMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;

@Configuration
public class SakOgAktivitetWSConfig {
    public static final String SAKOGAKTIVITET_KEY = "start.sakogaktivitet.withmock";
    @Value("${soknad.webservice.arena.sakogaktivitet.url}")
    private String sakOgAktivitetEndpoint;

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetInformasjonEndpoint() {
        SakOgAktivitetV1 mock = sakOgAktivitetInformasjonEndpointMock();
        SakOgAktivitetV1 prod = sakOgAktivitetInformasjonEndpointWS();
        return createSwitcher(prod, mock, SAKOGAKTIVITET_KEY, SakOgAktivitetV1.class);
    }
    private ServiceBuilder<SakOgAktivitetV1>.PortTypeBuilder<SakOgAktivitetV1> factory() {
        return new ServiceBuilder<>(SakOgAktivitetV1.class)
                .asStandardService()
                .withAddress(sakOgAktivitetEndpoint)
                .withWsdl("classpath:/no/nav/tjeneste/virksomhet/sakOgAktivitet/v1/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/sakOgAktivitet/v1/Binding", "SakOgAktivitet_v1"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/sakOgAktivitet/v1/Binding", "sakOgAktivitet_v1Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetInformasjonEndpointMock() {
        return new AktiviteterMock().sakOgAktivitetInformasjonV1Mock();
    }
    @Bean
    public SakOgAktivitetV1 sakOgAktivitetInformasjonEndpointWS() {
        return factory().withUserSecurity().get();
    }
}
