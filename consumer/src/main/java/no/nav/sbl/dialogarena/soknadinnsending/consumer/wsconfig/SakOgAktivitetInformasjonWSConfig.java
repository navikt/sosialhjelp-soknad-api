package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.AktiviteterMock;
import no.nav.tjeneste.virksomhet.sakogaktivitetinformasjon.v1.SakOgAktivitetInformasjonV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SakOgAktivitetInformasjonWSConfig {

    @Bean
    public SakOgAktivitetInformasjonV1 sakOgAktivitetInformasjonEndpoint() {
        return new AktiviteterMock().sakOgAktivitetInformasjonV1Mock();
    }
}
