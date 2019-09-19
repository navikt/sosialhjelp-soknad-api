package no.nav.sbl.dialogarena.config;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.bostotte.BostotteConfig;
import no.nav.sbl.dialogarena.oidc.OidcTokenValidatorConfig;
import no.nav.sbl.dialogarena.service.SaksoversiktMetadataService;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.sbl.dialogarena.virusscan.VirusScanConfig;
import org.springframework.context.annotation.*;

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
        VirusScanConfig.class,
        BostotteConfig.class
})
@ComponentScan(basePackages = "no.nav.sbl.dialogarena.rest")
public class SoknadinnsendingConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }
}