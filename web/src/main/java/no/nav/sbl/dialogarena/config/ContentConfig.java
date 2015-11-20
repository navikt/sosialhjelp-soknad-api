package no.nav.sbl.dialogarena.config;

import no.nav.modig.content.Content;
import no.nav.modig.content.ContentRetriever;
import no.nav.modig.content.enonic.HttpContentRetriever;
import no.nav.modig.content.enonic.innholdstekst.Innholdstekst;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.types.Pingable;
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

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
                new NavMessageSource.Bundle("aap", brukerprofilDataDirectoryString + "enonic/aap", "classpath:content/aap"),
                new NavMessageSource.Bundle("bilstonad", brukerprofilDataDirectoryString + "enonic/bilstonad", "classpath:content/bilstonad"),
                new NavMessageSource.Bundle("soknadtilleggsstonader", brukerprofilDataDirectoryString + "enonic/tilleggsstonader", "classpath:content/tilleggsstonader"),
                new NavMessageSource.Bundle("tiltakspenger", brukerprofilDataDirectoryString + "enonic/tiltakspenger", "classpath:content/tiltakspenger"),
                new NavMessageSource.Bundle("refusjondagligreise", brukerprofilDataDirectoryString + "enonic/refusjondagligreise", "classpath:content/refusjondagligreise")
        );

        messageSource.setDefaultEncoding("UTF-8");

        //Sjekk for nye filer en gang hvert 30. minutt.
        messageSource.setCacheSeconds(60 * 30);
        return messageSource;
    }

    //Hent innholdstekster på nytt hver time
    @Scheduled(cron = "0 0 * * * *")
    public void lastInnNyeInnholdstekster() {
        logger.debug("Leser inn innholdstekster fra enonic");
        clearContentCache();
        try {
            saveLocal("enonic/sendsoknad_nb_NO.properties", new URI(cmsBaseUrl + "/app/sendsoknad/nb_NO/tekster"));
            saveLocal("enonic/sendsoknad_en.properties", new URI(cmsBaseUrl + "/app/sendsoknad/en/tekster"));
            saveLocal("enonic/dagpenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/dagpenger/nb_NO/tekster"));
            saveLocal("enonic/dagpenger_en.properties", new URI(cmsBaseUrl + "/app/dagpenger/en/tekster"));
            saveLocal("enonic/foreldrepenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/foreldrepenger/nb_NO/tekster"));
            saveLocal("enonic/aap_nb_NO.properties", new URI(cmsBaseUrl + "/app/AAP/nb_NO/tekster"));
            saveLocal("enonic/bilstonad_nb_NO.properties", new URI(cmsBaseUrl + "/app/bilstonad/nb_NO/tekster"));
            saveLocal("enonic/tilleggsstonader_nb_NO.properties", new URI(cmsBaseUrl + "/app/tilleggsstonader/nb_NO/tekster"));
            saveLocal("enonic/tiltakspenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/tiltakspenger/nb_NO/tekster"));
            saveLocal("enonic/refusjonsdagligreise_nb_NO.properties", new URI(cmsBaseUrl + "/app/refusjondagligreise/nb_NO/tekster"));
        } catch (Exception e) {
            logger.warn("Feilet under henting av enonic innholdstekster: " + e, e);
        }
        navMessageSource().clearCache();
    }

    @Bean
    public Pingable cmsPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                String url = "";
                HttpURLConnection connection = null;
                try {
                    url = System.getProperty("dialogarena.cms.url");
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(10000);
                    if (connection.getResponseCode() == HTTP_OK) {
                        return Ping.lyktes("APPRES_CMS");
                    } else {
                        throw new ApplicationException("Fikk feilkode fra CMS: " + connection.getResponseCode() + ": " + connection.getResponseMessage());
                    }
                } catch (Exception e) {
                    logger.warn("CMS not reachable on " + url, e);
                    return Ping.feilet("APPRES_CMS", e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        };
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
                data.append(entry.getValue().key).append('=').append(removeNewline(entry.getValue().value)).append(System.lineSeparator());
            }
            FileUtils.write(file, data, "UTF-8");
        }
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