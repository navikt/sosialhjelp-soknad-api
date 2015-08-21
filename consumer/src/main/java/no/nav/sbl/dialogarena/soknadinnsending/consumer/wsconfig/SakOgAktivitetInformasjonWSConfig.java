package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.AktiviteterMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SakOgAktivitetInformasjonWSConfig {

    @Bean
    public SakOgAktivitetV1 sakOgAktivitetInformasjonEndpoint() {
        return new AktiviteterMock().sakOgAktivitetInformasjonV1Mock();
    }

    @Bean
    Pingable sakOgAktivitetPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                try {
                    sakOgAktivitetInformasjonEndpoint().ping();
                    return Ping.lyktes("SakOgAktivitet");
                } catch (Exception e) {
                    return Ping.feilet("SakOgAktivitet", e);
                }
            }
        };
    }
}
