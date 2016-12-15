package no.nav.sbl.dialogarena.config;

import no.nav.modig.content.Content;
import no.nav.modig.content.ContentRetriever;
import no.nav.modig.content.enonic.HttpContentRetriever;
import no.nav.modig.content.enonic.innholdstekst.Innholdstekst;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.types.Pingable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

@Configuration
@EnableScheduling
public class ContentConfig {
    private final static int TI_MINUTTER = 1000 * 60 * 10;

    @Value("${dialogarena.cms.url}")
    private String cmsBaseUrl;

    @Value("${sendsoknad.datadir}")
    private File brukerprofilDataDirectory;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    final static String delstiTilbundlefilPaaDisk = "/tekster";

    @Bean
    public NavMessageSource navMessageSource() {
        //Vi lager en reloadablemessagesource som henter både fra lokal disk og fra classpath. Se lastInnNyeInnholdstekster for å se koden som skriver de filene som hentes fra enonic.
        NavMessageSource messageSource = new NavMessageSource();

        String brukerprofilDataDirectoryString = brukerprofilDataDirectory.toURI().toString();



        NavMessageSource.Bundle[] bundles = new NavMessageSource.Bundle[kravdialogInformasjonHolder.getSoknadsKonfigurasjoner().size()];
        int index = 0;
        for (KravdialogInformasjon kravdialogInformasjon : kravdialogInformasjonHolder.getSoknadsKonfigurasjoner()) {
            bundles[index++] = (getBundle(kravdialogInformasjon.getBundleName(), kravdialogInformasjon.brukerEnonic(), brukerprofilDataDirectoryString));
        }

        NavMessageSource.Bundle fellesBundle = new NavMessageSource.Bundle("sendsoknad", brukerprofilDataDirectoryString + "enonic/sendsoknad", "classpath:content/sendsoknad");

        messageSource.setBasenames(fellesBundle, bundles);
        messageSource.setDefaultEncoding("UTF-8");

        //Sjekk for nye filer en gang hvert 15. sekund.
        messageSource.setCacheSeconds(15);
        return messageSource;
    }

    @Bean
    public NavMessageWrapper navMessageBundles() {
        NavMessageWrapper messages = new NavMessageWrapper();
        for (KravdialogInformasjon kravdialogInformasjon : kravdialogInformasjonHolder.getSoknadsKonfigurasjoner()) {
            messages.put(kravdialogInformasjon.getSoknadTypePrefix(), bundleFor(kravdialogInformasjon.getBundleName(), kravdialogInformasjon.brukerEnonic()));
        }
        return messages;
    }

    public static class NavMessageWrapper extends HashMap<String, MessageSource>{}

    private NavMessageSource bundleFor(String bundleName, boolean brukerEnonic) {
        NavMessageSource messageSource = new NavMessageSource();

        String brukerprofilDataDirectoryString = brukerprofilDataDirectory.toURI().toString();
        NavMessageSource.Bundle dialogBundle;

        dialogBundle = getBundle(bundleName, brukerEnonic, brukerprofilDataDirectoryString);

        NavMessageSource.Bundle fellesBundle = new NavMessageSource.Bundle("sendsoknad", brukerprofilDataDirectoryString + "enonic/sendsoknad", "classpath:content/sendsoknad");

        messageSource.setBasenames(
                fellesBundle,
                dialogBundle
        );
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    private NavMessageSource.Bundle getBundle(String bundleName, boolean brukerEnonic, String brukerprofilDataDirectoryString) {
        NavMessageSource.Bundle dialogBundle;
        if (brukerEnonic) {
            dialogBundle = new NavMessageSource.Bundle(bundleName, brukerprofilDataDirectoryString + "enonic/" + bundleName, "classpath:content/" + bundleName);
        } else {
            dialogBundle = new NavMessageSource.Bundle(bundleName, System.getProperty("folder." + bundleName + ".path") + delstiTilbundlefilPaaDisk + "/" + bundleName, null);
        }
        return dialogBundle;
    }

    //Hent innholdstekster på nytt hvert tiende minutt
    @Scheduled(fixedRate = TI_MINUTTER)
    public void lastInnNyeInnholdstekster() {
        logger.info("Leser inn innholdstekster fra enonic");
        clearContentCache();
        try {
            saveLocal("enonic/sendsoknad_nb_NO.properties", new URI(cmsBaseUrl + "/app/sendsoknad/nb_NO/tekster"));
            saveLocal("enonic/sendsoknad_en.properties", new URI(cmsBaseUrl + "/app/sendsoknad/en/tekster"));
            saveLocal("enonic/dagpenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/dagpenger/nb_NO/tekster"));
            saveLocal("enonic/dagpenger_en.properties", new URI(cmsBaseUrl + "/app/dagpenger/en/tekster"));
            saveLocal("enonic/aap_nb_NO.properties", new URI(cmsBaseUrl + "/app/AAP/nb_NO/tekster"));
            saveLocal("enonic/bilstonad_nb_NO.properties", new URI(cmsBaseUrl + "/app/bilstonad/nb_NO/tekster"));
            saveLocal("enonic/tilleggsstonader_nb_NO.properties", new URI(cmsBaseUrl + "/app/tilleggsstonader/nb_NO/tekster"));
            saveLocal("enonic/tiltakspenger_nb_NO.properties", new URI(cmsBaseUrl + "/app/tiltakspenger/nb_NO/tekster"));
            saveLocal("enonic/refusjondagligreise_nb_NO.properties", new URI(cmsBaseUrl + "/app/refusjondagligreise/nb_NO/tekster"));
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
        logger.info("Leser inn innholdstekster fra " + uri + " til: " + file.toString());
        Content<Innholdstekst> content = enonicContentRetriever().getContent(uri);
        StringBuilder data = new StringBuilder();
        Map<String, Innholdstekst> innhold = content.toMap(Innholdstekst.KEY);
        if (!innhold.isEmpty()) {
            Map<String, String> cmsChangeMap = getCmsChangeMap(filename);
            for (Map.Entry<String, Innholdstekst> entry : innhold.entrySet()) {
                String key = entry.getValue().key;
                if (cmsChangeMap.containsKey(key)) {
                    data.append(key).append('=').append("[BYTTET NAVN] ").append(key).append("->").append(cmsChangeMap.get(key)).append(System.lineSeparator());
                    key = cmsChangeMap.get(key);
                }
                data.append(key).append('=').append(removeNewline(entry.getValue().value)).append(System.lineSeparator());
            }
            FileUtils.write(file, data.toString(), "UTF-8");
        }
    }

    private static Map<String, String> getCmsChangeMap(String filename) throws IOException {
        String mappingFileName = filename.substring(0, filename.indexOf('_')).replaceAll("enonic", "content") + ".properties.mapping";
        InputStream mapping = ContentConfig.class.getResourceAsStream("/" + mappingFileName);
        Map<String, String> changes = new HashMap<>();
        if (mapping != null) {
            List<String> strings = IOUtils.readLines(mapping, "UTF-8");
            for (String string : strings) {
                if (string.split("=").length == 2) {
                    changes.put(string.split("=")[0], string.split("=")[1]);
                }
            }
        }
        return changes;
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