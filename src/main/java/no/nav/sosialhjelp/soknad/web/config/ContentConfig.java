package no.nav.sosialhjelp.soknad.web.config;

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import static no.nav.sosialhjelp.soknad.tekster.BundleNameKt.BUNDLE_NAME;

@Configuration
@EnableScheduling
public class ContentConfig {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long FEM_MINUTTER = 1000*60*5;

    @Value("${scheduler.disable}")
    private boolean schedulerDisabled;

    @Bean
    public NavMessageSource navMessageSource() {
        NavMessageSource messageSource = new NavMessageSource();

        NavMessageSource.Bundle bundle = getBundle(BUNDLE_NAME);
        NavMessageSource.Bundle fellesBundle = getBundle("sendsoknad");

        messageSource.setBasenames(fellesBundle, bundle);
        messageSource.setDefaultEncoding("UTF-8");

        //Sjekk for nye filer en gang hvert 15. sekund.
        messageSource.setCacheSeconds(15);
        return messageSource;
    }

    @Scheduled(fixedRate = FEM_MINUTTER)
    private void slettCache() {
        if (schedulerDisabled) {
            logger.info("Scheduler is disabled");
            return;
        }

        navMessageSource().clearCache();
    }

    private NavMessageSource.Bundle getBundle(String bundleName) {
        return new NavMessageSource.Bundle(bundleName, "classpath:/" + bundleName);
    }
}
