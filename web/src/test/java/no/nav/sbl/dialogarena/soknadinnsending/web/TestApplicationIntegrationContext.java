package no.nav.sbl.dialogarena.soknadinnsending.web;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.ServicesApplicationContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MockConsumerConfig;
import no.nav.sbl.dialogarena.websoknad.config.ApplicationContext;
import no.nav.sbl.dialogarena.websoknad.config.ContentConfig;
import no.nav.sbl.dialogarena.websoknad.config.FooterConfig;
import no.nav.sbl.dialogarena.websoknad.config.GAConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Import({
        MockConsumerConfig.class,
        ServicesApplicationContext.class,
        ApplicationContext.class,
        BusinessConfig.class,
        CacheConfig.class,
        FooterConfig.class,
        GAConfig.class,
        ContentConfig.class,
        SoknadInnsendingDBConfig.class,
        ConsumerConfig.SendSoknadWSConfig.class,
        ConsumerConfig.FilLagerWSConfig.class
})

public class TestApplicationIntegrationContext {
    private boolean mockBrukerprofil = true;
    private boolean mockTps = true;
    private boolean mockKodeverk = true;
    @Inject
    @SuppressWarnings("PMD")
    private org.springframework.context.ApplicationContext context;

    @Bean
    public Object brukerprofilServiceCreator() {
        if (mockBrukerprofil) {
            return new MockConsumerConfig.BrukerProfilWSConfig();
        } else {
            return new ConsumerConfig.BrukerProfilWSConfig();
        }
    }

    @Bean
    public Object kodeverkServiceCreator() {
        if (mockKodeverk) {
            return new MockConsumerConfig.KodeverkWSConfig();
        } else {
            return new ConsumerConfig.KodeverkWSConfig();
        }
    }

    @Bean
    public Object personServiceCreator() {

        if (mockTps) {
            return new MockConsumerConfig.PersonWSConfig();
        } else {
            return new ConsumerConfig.PersonWSConfig();
        }
    }

    @Bean
    public Object aktorServiceCreator() {

        if (mockTps) {
            return new MockConsumerConfig.AktorWsConfig();
        } else {
            return new ConsumerConfig.AktorWsConfig();
        }
    }

}
