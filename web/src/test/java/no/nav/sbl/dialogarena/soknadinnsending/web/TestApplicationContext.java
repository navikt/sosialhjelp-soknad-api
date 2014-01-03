package no.nav.sbl.dialogarena.soknadinnsending.web;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.ServicesApplicationContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfigTest;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MockConsumerConfig;
import no.nav.sbl.dialogarena.websoknad.config.ApplicationContext;
import no.nav.sbl.dialogarena.websoknad.config.ContentConfig;
import no.nav.sbl.dialogarena.websoknad.config.FooterConfig;
import no.nav.sbl.dialogarena.websoknad.config.GAConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ServicesApplicationContext.class,
        ApplicationContext.class,
        BusinessConfig.class,
        CacheConfig.class,
        FooterConfig.class,
        GAConfig.class,
        ContentConfig.class,
        ConsumerConfigTest.class,
        SoknadInnsendingDBConfig.class})

public class TestApplicationContext {
    private boolean mockHenvendelse = true;
    private boolean mockBrukerprofil = true;
    private boolean mockTps = true;
    private boolean mockKodeverk = false;

    @Bean
    public String henvendelseServiceCreator() {
        if (mockHenvendelse) {
            new MockConsumerConfig.SendSoknadWSConfig();
            new MockConsumerConfig.FilLagerWSConfig();
        } else {
            new ConsumerConfig.SendSoknadWSConfig();
            new ConsumerConfig.FilLagerWSConfig();
        }
        return "henvendelse";
    }

    @Bean
    public String brukerprofilServiceCreator() {
        if (mockBrukerprofil) {
            new MockConsumerConfig.BrukerProfilWSConfig();
        } else {
            new ConsumerConfig.BrukerProfilWSConfig();
        }
        return "brukerprofil";
    }

    @Bean
    public String kodeverkServiceCreator() {
        System.out.println("starter kodeverk");
        if (mockKodeverk) {
            new MockConsumerConfig.KodeverkWSConfig();
        } else {
            new ConsumerConfig.KodeverkWSConfig();
        }
        return "kodeverk";
    }

    @Bean
    public Object tpsServiceCreator() {
        if (mockTps) {
            new MockConsumerConfig.PersonWSConfig();
            new MockConsumerConfig.AktorWsConfig();
        } else {
            new ConsumerConfig.PersonWSConfig();
            new ConsumerConfig.AktorWsConfig();
        }
        return "tps";
    }

}
