package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.DefaultSubjectHandlerWrapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.service.SaksoversiktMetadataService;
import no.nav.sbl.dialogarena.service.SoknadOversiktService;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.BostotteConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.DigisosApiRestConfig;
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
//        OidcTokenValidatorConfig.class,
        MetricsConfig.class,
        SaksoversiktMetadataService.class,
        SoknadOversiktService.class,
        VirusScanConfig.class,
        DigisosApiRestConfig.class,
        BostotteConfig.class,
        DefaultSubjectHandlerWrapper.class,
        XsrfGenerator.class
})
@ComponentScan(basePackages = "no.nav.sbl.dialogarena.rest")
public class SoknadinnsendingConfig {

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }
}