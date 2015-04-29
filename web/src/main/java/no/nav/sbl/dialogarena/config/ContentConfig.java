package no.nav.sbl.dialogarena.config;

import no.nav.modig.content.Content;
import no.nav.modig.content.ContentRetriever;
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
import java.util.Map;

@Configuration
public class ContentConfig {

    @Value("${dialogarena.cms.url}")
    private String cmsBaseUrl;

    @Value("${sendsoknad.datadir}")
    private File brukerprofilDataDirectory;

    @Inject
    private CacheManager cacheManager;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public NavMessageSource navMessageSource() {
        //Vi lager en reloadablemessagesource som henter både fra lokal disk og fra classpath. Se lastInnNyeInnholdstekster for å se koden som skriver de filene som hentes fra enonic.
        NavMessageSource messageSource = new NavMessageSource();

        String brukerprofilDataDirectoryString = brukerprofilDataDirectory.toURI().toString();

        messageSource.setBasenames(
                new NavMessageSource.Bundle("sendsoknad", brukerprofilDataDirectoryString + "enonic/sendsoknad", "classpath:content/sendsoknad"),
                new NavMessageSource.Bundle("dagpenger", brukerprofilDataDirectoryString + "enonic/dagpenger", "classpath:content/dagpenger"),
                new NavMessageSource.Bundle("foreldrepenger", brukerprofilDataDirectoryString + "enonic/foreldrepenger", "classpath:content/foreldrepenger"),
                new NavMessageSource.Bundle("aap_innholdstekster", brukerprofilDataDirectoryString + "enonic/aap_innholdstekster", "classpath:content/aap_innholdstekster")
        );

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
            saveLocal("enonic/sendsoknad_nb_NO.properties", new URI(cmsBaseUrl + "/app/sendsoknad/nb_NO/tekster"));
            saveLocal("enonic/sendsoknad_en_GB.properties", new URI(cmsBaseUrl + "/app/sendsoknad/en_GB/tekster"));
            saveLocal("enonic/dagpenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/dagpenger/nb_NO/tekster"));
            saveLocal("enonic/dagpenger_en_GB.properties", new URI(cmsBaseUrl + "/app/dagpenger/en_GB/tekster"));
            saveLocal("enonic/foreldrepenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/foreldrepenger/nb_NO/tekster"));
            saveLocal("enonic/foreldrepenger_en_GB.properties", new URI(cmsBaseUrl + "/app/foreldrepenger/en_GB/tekster"));
            saveLocal("enonic/aap_innholdstekster_nb_NO.properties", new URI(cmsBaseUrl + "/app/AAP/nb_NO/tekster"));
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
}