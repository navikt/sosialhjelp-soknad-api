package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.innholdshenter.common.EnonicContentRetriever;
import no.nav.innholdshenter.filter.DecoratorFilter;
import no.nav.modig.content.*;
import no.nav.modig.content.enonic.HttpContentRetriever;
import no.nav.modig.content.enonic.innholdstekst.Innholdstekst;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.websoknad.config.StripPTagsPropertyPersister.stripPTag;

@Configuration
public class ContentConfig {

    @Value("${dialogarena.cms.url}")
    private String cmsBaseUrl;
    @Value("${sendsoknad.datadir}")
    private File brukerprofilDataDirectory;
    @Inject
    private CacheManager cacheManager;


    private static final String DEFAULT_LOCALE = "nb";
    private static final String INNHOLDSTEKSTER_NB_NO_REMOTE = "/app/sendsoknad/bm/tekster";
    private static final String INNHOLDSTEKSTER_NB_NO_LOCAL = "content.innholdstekster";
    private static final String SBL_WEBKOMPONENTER_NB_NO_REMOTE = "/app/sbl-webkomponenter/nb/tekster";
    private static final String SBL_WEBKOMPONENTER_NB_NO_LOCAL = "content.sbl-webkomponenter";
    private static final String FRAGMENTS_URL = "common-html/v1/navno";
    private static final List<String> NO_DECORATOR_PATTERNS = new ArrayList<>(asList(".*/img/.*", ".*selftest.*"));
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Bean
    public ValueRetriever siteContentRetriever() throws URISyntaxException {
        Map<String, List<URI>> uris = new HashMap<>();
        uris.put(DEFAULT_LOCALE,
                asList(
                        new URI(cmsBaseUrl + INNHOLDSTEKSTER_NB_NO_REMOTE),
                        new URI(cmsBaseUrl + SBL_WEBKOMPONENTER_NB_NO_REMOTE)
                ));
        return new ValuesFromContentWithResourceBundleFallback(
                asList(INNHOLDSTEKSTER_NB_NO_LOCAL, SBL_WEBKOMPONENTER_NB_NO_LOCAL),
                enonicContentRetriever(),
                uris,
                DEFAULT_LOCALE);
    }

    @Bean
    public NavMessageSource navMessageSource() {
        //Vi lager en reloadablemessagesource som henter både fra lokal disk og fra classpath. Se lastInnNyeInnholdstekster for å se koden som skriver de filene som hentes fra enonic.
        NavMessageSource messageSource = new NavMessageSource();
        messageSource.setBasenames(
                new File(brukerprofilDataDirectory, "enonic/innholdstekster").toURI().toString(),
                new File(brukerprofilDataDirectory, "enonic/sbl-webkomponenter").toURI().toString(),
                "classpath:content/innholdstekster", "classpath:content/sbl-webkomponenter");
        messageSource.setDefaultEncoding("UTF-8");
        //Sjekk for nye filer en gang hvert 30. minutt.
        messageSource.setCacheSeconds(60 * 30);
        messageSource.setPropertiesPersister(new StripPTagsPropertyPersister());
        return messageSource;
    }

    //Hent innholdstekster på nytt hver time
    @Scheduled(cron = "0 0 * * * *")
    public void lastInnNyeInnholdstekster() {
        logger.debug("Leser inn innholdstekster fra enonic");
        clearContentCache();
        try {
            saveLocal("enonic/innholdstekster_nb.properties", new URI(cmsBaseUrl + INNHOLDSTEKSTER_NB_NO_REMOTE));
            saveLocal("enonic/sbl-webkomponenter_nb", new URI(cmsBaseUrl + SBL_WEBKOMPONENTER_NB_NO_REMOTE));
        } catch (Exception e) {
            logger.warn("Feilet under henting av enonic innholdstekster: " + e, e);
        }
        navMessageSource().clearCache();
    }

    private void clearContentCache() {
        cacheManager.getCache("cms.content").clear();
        cacheManager.getCache("cms.article").clear();
    }

    private void saveLocal(String filename, URI uri) throws IOException {
        File file = new File(brukerprofilDataDirectory, filename);
        logger.debug("Leser inn innholdstekster fra " + uri + " til: " + file.toString());
        Content<Innholdstekst> content = enonicContentRetriever().getContent(uri);
        StringBuilder data = new StringBuilder();
        Map<String, Innholdstekst> innhold = content.toMap(Innholdstekst.KEY);
        if (!innhold.isEmpty()) {
            for (Map.Entry<String, Innholdstekst> entry : innhold.entrySet()) {
                data.append(entry.getValue().key).append("=").append(stripPTag(entry.getValue().value)).append(System.lineSeparator());
            }
            FileUtils.write(file, data, "UTF-8");
        }
    }

    @Bean
    public ContentRetriever enonicContentRetriever() {
        HttpContentRetriever httpContentRetriever = new HttpContentRetriever();
        httpContentRetriever.http.setTimeout(20 * 1000);
        return httpContentRetriever;
    }

    @Bean
    public CmsContentRetriever cmsContentRetriever() throws URISyntaxException {
        CmsContentRetriever cmsContentRetriever = new CmsContentRetriever();
        cmsContentRetriever.setDefaultLocale(DEFAULT_LOCALE);
        cmsContentRetriever.setTeksterRetriever(siteContentRetriever());
        cmsContentRetriever.setArtikkelRetriever(siteContentRetriever());
        return cmsContentRetriever;
    }

    @Bean(name = "cmsBaseUrl")
    public String cmsBaseUrl() {
        return cmsBaseUrl;
    }


    @Bean
    public DecoratorFilter decoratorFilter() {
        DecoratorFilter decorator = new DecoratorFilter();
        decorator.setFragmentsUrl(FRAGMENTS_URL);
        decorator.setContentRetriever(appresContentRetriever());
        decorator.setApplicationName("Saksoversikt");
        decorator.setNoDecoratePatterns(NO_DECORATOR_PATTERNS);
        decorator.setFragmentNames(asList(
                "header-withmenu",
                "footer-withmenu"
        ));
        return decorator;
    }

    private EnonicContentRetriever appresContentRetriever() {
        EnonicContentRetriever contentRetriever = new EnonicContentRetriever("saksoversikt");
        contentRetriever.setBaseUrl(cmsBaseUrl);
        contentRetriever.setRefreshIntervalSeconds(1800);
        contentRetriever.setHttpTimeoutMillis(10000);
        return contentRetriever;
    }
}