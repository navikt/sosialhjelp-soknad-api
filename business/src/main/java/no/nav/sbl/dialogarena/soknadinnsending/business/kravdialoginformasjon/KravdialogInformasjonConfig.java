package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class KravdialogInformasjonConfig {

    @Bean
    public KravdialogInformasjonHolder kravdialogInformasjonHolder() {
        return new KravdialogInformasjonHolder();
    }

}
