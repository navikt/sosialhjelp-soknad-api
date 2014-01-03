package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.modig.cache.CacheConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Applikasjonskontekst for ear-modulen.
 */
@Configuration
@Import({
        ApplicationContext.class,
        BusinessConfig.class,
        CacheConfig.class,
        FooterConfig.class,
        GAConfig.class,
        ConsumerConfig.class,
        ContentConfig.class,
        SoknadInnsendingDBConfig.class})
public class SoknadinnsendingContext {

}