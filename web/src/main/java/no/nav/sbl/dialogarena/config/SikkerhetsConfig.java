package no.nav.sbl.dialogarena.config;

import no.nav.common.oidc.auth.OidcAuthenticationFilter;
import no.nav.sbl.dialogarena.server.SosialhjelpAuthenticationFilter;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class SikkerhetsConfig {

    final List<String> publicPath = Collections.singletonList("/*");

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }

    @Bean("sendSoknadAuthenticationFilter")
    public SosialhjelpAuthenticationFilter authenticationFilter() {
        return new SosialhjelpAuthenticationFilter();
    }

}
