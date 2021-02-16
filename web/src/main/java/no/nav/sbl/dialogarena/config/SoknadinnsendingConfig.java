package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.oidc.OidcTokenValidatorConfig;
import no.nav.sbl.dialogarena.service.SaksoversiktMetadataService;
import no.nav.sbl.dialogarena.service.SoknadOversiktService;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
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
@ComponentScan(basePackages = "no.nav.sbl.dialogarena.rest")
public class SoknadinnsendingConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }
}