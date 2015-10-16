package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.mockmodul.tjenester.MaalgrupperMock;
import no.nav.tjeneste.virksomhet.maalgruppeinformasjon.v1.MaalgruppeinformasjonV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaalgruppeinformasjonWSConfig {

    @Bean
    public MaalgruppeinformasjonV1 maalgruppeinformasjonEndpoint() {
        return MaalgrupperMock.maalgruppeinformasjonV1();
    }
}
