package no.nav.sbl.dialogarena.config;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.inject.Inject;
import java.io.File;


@Configuration
@EnableScheduling
public class ContentConfig {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    final static String delstiTilbundlefilPaaDisk = "tekster";

    @Bean
    public NavMessageSource navMessageSource() {
        NavMessageSource messageSource = new NavMessageSource();

        NavMessageSource.Bundle[] bundles = new NavMessageSource.Bundle[kravdialogInformasjonHolder.getSoknadsKonfigurasjoner().size()];
        int index = 0;
        for (KravdialogInformasjon kravdialogInformasjon : kravdialogInformasjonHolder.getSoknadsKonfigurasjoner()) {
            bundles[index++] = getBundle(kravdialogInformasjon.getBundleName());
        }

        NavMessageSource.Bundle fellesBundle = getBundle("sendsoknad");

        messageSource.setBasenames(fellesBundle, bundles);
        messageSource.setDefaultEncoding("UTF-8");

        //Sjekk for nye filer en gang hvert 15. sekund.
        messageSource.setCacheSeconds(15);
        return messageSource;
    }

    private NavMessageSource.Bundle getBundle(String bundleName) {
        NavMessageSource.Bundle dialogBundle;
        final String remoteFile = new File(System.getProperty("folder." + bundleName + ".path")).toURI().toString() + delstiTilbundlefilPaaDisk + "/" + bundleName;
        dialogBundle = new NavMessageSource.Bundle(bundleName, remoteFile, null);
        return dialogBundle;
    }
}