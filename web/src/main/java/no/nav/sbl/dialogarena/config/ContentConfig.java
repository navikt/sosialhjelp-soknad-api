package no.nav.sbl.dialogarena.config;


import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.io.File;


@Configuration
@EnableScheduling
public class ContentConfig {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    final static String delstiTilbundlefilPaaDisk = "tekster";
    private static final long FEM_MINUTTER = 1000*60*5;

    @Bean
    public NavMessageSource navMessageSource() {
        NavMessageSource messageSource = new NavMessageSource();

        NavMessageSource.Bundle[] bundles = new NavMessageSource.Bundle[kravdialogInformasjonHolder.getSoknadsKonfigurasjoner().size()];
        int index = 0;

        for (KravdialogInformasjon kravdialogInformasjon : kravdialogInformasjonHolder.getSoknadsKonfigurasjoner()) {
            String property = System.getProperty(getPropertyKey(kravdialogInformasjon.getBundleName()));
            if( property == null ) {
                throw new RuntimeException("Property: " + getPropertyKey(kravdialogInformasjon.getBundleName()) + " finnes ikke");
            }
            bundles[index++] = getBundle(kravdialogInformasjon.getBundleName());
        }

        NavMessageSource.Bundle fellesBundle = getBundle("sendsoknad");

        messageSource.setBasenames(fellesBundle, bundles);
        messageSource.setDefaultEncoding("UTF-8");

        //Sjekk for nye filer en gang hvert 15. sekund.
        messageSource.setCacheSeconds(15);
        return messageSource;
    }

    @Scheduled(fixedRate = FEM_MINUTTER)
    private void slettCache() {
        navMessageSource().clearCache();
    }

    private String getPropertyKey(String bundleName) {
        return "folder." + bundleName + ".path";
    }

    private NavMessageSource.Bundle getBundle(String bundleName) {
        NavMessageSource.Bundle dialogBundle;
        final String remoteFile = new File(System.getProperty(getPropertyKey(bundleName))).toURI().toString() + delstiTilbundlefilPaaDisk + "/" + bundleName;
        dialogBundle = new NavMessageSource.Bundle(bundleName, remoteFile, null);
        return dialogBundle;
    }
}