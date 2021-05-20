package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.business.BusinessConfig;
import no.nav.sosialhjelp.soknad.business.db.config.SoknadInnsendingDBConfig;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.consumer.ConsumerConfig;
import no.nav.sosialhjelp.soknad.consumer.bostotte.BostotteConfig;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PersonService;
import no.nav.sosialhjelp.soknad.consumer.restconfig.DigisosApiRestConfig;
import no.nav.sosialhjelp.soknad.consumer.virusscan.VirusScanConfig;
import no.nav.sosialhjelp.soknad.web.oidc.OidcTokenValidatorConfig;
import no.nav.sosialhjelp.soknad.web.service.SaksoversiktMetadataService;
import no.nav.sosialhjelp.soknad.web.service.SoknadOversiktService;
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll;
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
    public Tilgangskontroll tilgangskontroll(SoknadMetadataRepository soknadMetadataRepository, SoknadUnderArbeidRepository soknadUnderArbeidRepository, PersonService personService) {
        return new Tilgangskontroll(soknadMetadataRepository, soknadUnderArbeidRepository, personService);
    }
}