package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.AktiviteterMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.SakOgAktivitetInformasjonV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.util.InstanceSwitcher.createSwitcher;

@Configuration
public class SakOgAktivitetInformasjonWSConfig {
    public static final String SAKOGAKTIVITET_KEY = "start.sakogaktivitet.withmock";
    @Value("${soknad.webservice.arena.sakogaktivitet.url}")
    private String sakOgAktivitetEndpoint;

    @Bean
    public SakOgAktivitetInformasjonV1 sakOgAktivitetInformasjonEndpoint() {
        SakOgAktivitetInformasjonV1 mock = sakOgAktivitetInformasjonEndpointMock();
        SakOgAktivitetInformasjonV1 prod = sakOgAktivitetInformasjonEndpointWS();
        return createSwitcher(prod, mock, SAKOGAKTIVITET_KEY, SakOgAktivitetInformasjonV1.class);
    }
    private ServiceBuilder<SakOgAktivitetInformasjonV1>.PortTypeBuilder<SakOgAktivitetInformasjonV1> factory() {
        return new ServiceBuilder<>(SakOgAktivitetInformasjonV1.class)
                .asStandardService()
                .withAddress(sakOgAktivitetEndpoint)
                .withWsdl("classpath:/no/nav/tjeneste/virksomhet/sakOgAktivitetInformasjon/v1/Binding.wsdl")
                .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/sakOgAktivitetInformasjon/v1/Binding", "SakOgAktivitetInformasjon_v1"))
                .withEndpointName(new QName("http://nav.no/tjeneste/virksomhet/sakOgAktivitetInformasjon/v1/Binding", "sakOgAktivitetInformasjon_v1Port"))
                .build()
                .withHttpsMock()
                .withMDC();
    }

    @Bean
    public SakOgAktivitetInformasjonV1 sakOgAktivitetInformasjonEndpointMock() {
        return new AktiviteterMock().sakOgAktivitetInformasjonV1Mock();
    }
    @Bean
    public SakOgAktivitetInformasjonV1 sakOgAktivitetInformasjonEndpointWS() {
        return factory().withUserSecurity().get();
    }
}
