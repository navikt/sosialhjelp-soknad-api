package no.nav.sbl.dialogarena.config;

import no.nav.modig.content.*;
import no.nav.modig.content.enonic.HttpContentRetriever;
import no.nav.modig.content.enonic.innholdstekst.Innholdstekst;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.utils.StripPTagsPropertyPersister;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

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
    private static final String FORELDRESOKNAD_NB_NO_REMOTE = "/app/foreldrepenger/nb_NO/tekster";
    private static final String FORELDRESOKNAD_NB_NO_LOCAL = "content.foreldresoknad_innholdstekster";
    private static final String AAP_NB_NO_REMOTE = "/app/AAP/nb_NO/tekster";
    private static final String AAP_NB_NO_LOCAL = "content.aap_innholdstekster";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Bean
    public ValueRetriever siteContentRetriever() throws URISyntaxException {
        Map<String, List<URI>> uris = new HashMap<>();
        uris.put(DEFAULT_LOCALE,
                asList(
                        new URI(cmsBaseUrl + INNHOLDSTEKSTER_NB_NO_REMOTE),
                        new URI(cmsBaseUrl + FORELDRESOKNAD_NB_NO_REMOTE),
                        new URI(cmsBaseUrl + AAP_NB_NO_REMOTE)
                ));
        return new ValuesFromContentWithResourceBundleFallback(
                asList(INNHOLDSTEKSTER_NB_NO_LOCAL, FORELDRESOKNAD_NB_NO_LOCAL, AAP_NB_NO_LOCAL),
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
                new File(brukerprofilDataDirectory, "enonic/foreldresoknad_innholdstekster").toURI().toString(),
                new File(brukerprofilDataDirectory, "enonic/aap_innholdstekster").toURI().toString(),
                "classpath:content/innholdstekster",
                "classpath:content/foreldresoknad_innholdstekster",
                "classpath:content/aap_innholdstekster");

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
            saveLocal("enonic/foreldresoknad_innholdstekster_nb_NO.properties", new URI(cmsBaseUrl + FORELDRESOKNAD_NB_NO_REMOTE));
            saveLocal("enonic/aap_innholdstekster_nb_NO.properties", new URI(cmsBaseUrl + AAP_NB_NO_REMOTE));
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
                data.append(entry.getValue().key).append('=').append(formatText(entry.getValue().value)).append(System.lineSeparator());
            }
            FileUtils.write(file, data, "UTF-8");
        }
    }

    private String formatText(String value) {
        String resultValue;
        resultValue = stripParagraphTags(value);
        resultValue = removeNewline(resultValue);
        return resultValue;
    }

    private String stripParagraphTags(String value) {
        String res = value;
        if (value != null) {
            if (res.startsWith("<p>")) {
                res = res.substring(3);
            }
            if (res.endsWith("</p>")) {
                res = res.substring(0, res.length() - 4);
            }
        }
        return res;
    }

    private String removeNewline(String value) {
        return value.replaceAll("\n", "");
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
}