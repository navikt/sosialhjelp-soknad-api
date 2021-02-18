package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.web.oidc.OidcTokenValidatorConfig;
import no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService;
import no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
import no.nav.sosialhjelp.soknad.business.BusinessConfig;
import no.nav.sosialhjelp.soknad.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sosialhjelp.soknad.consumer.ConsumerConfig;
import no.nav.sosialhjelp.soknad.consumer.bostotte.BostotteConfig;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.consumer.virusscan.VirusScanConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@EnableAspectJAutoProxy
@Configuration
@Import({
        ApplicationConfig.class,
        BusinessConfig.class,
        CacheConfig.class,
        GAConfig.class,
        ConsumerConfig.class,
        ContentConfig.class,
        SoknadInnsendingDBConfig.class,
        HandlebarsHelperConfig.class,
        OidcTokenValidatorConfig.class,
        MetricsConfig.class,
        SaksoversiktMetadataService.class,
        SoknadOversiktService.class,
        VirusScanConfig.class,
        DigisosApiRestConfig.class,
        BostotteConfig.class
})
@ComponentScan(basePackages = "no.nav.sosialhjelp.soknad.web.rest")
public class SoknadinnsendingConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }
}