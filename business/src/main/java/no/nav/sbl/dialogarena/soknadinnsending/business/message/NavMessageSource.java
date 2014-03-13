package no.nav.sbl.dialogarena.soknadinnsending.business.message;

import no.nav.modig.content.Content;
import no.nav.modig.content.ContentRetriever;
import no.nav.modig.content.enonic.innholdstekst.Innholdstekst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import javax.inject.Inject;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


public class NavMessageSource extends ReloadableResourceBundleMessageSource {
    @Inject
    private ContentRetriever contentRetriever;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Map<String, String>> fileToEnonicMapping;
    private boolean enableEnonic;

    public void setEnonicMap(Map<String, Map<String, String>> enonicMap) {
        this.fileToEnonicMapping = enonicMap;
    }

    public void setEnableEnonic(boolean enable) {
        enableEnonic = enable;
    }

    public Properties getBundleFor(String prefix, Locale locale) {
        return getMergedProperties(locale).getProperties();
    }

    @Override
    protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {
        logger.warn("Starter refresh av" + filename);

        PropertiesHolder holder = super.refreshProperties(filename, propHolder);
        if (enableEnonic) {
            String[] fileSplit = filename.split("_");
            if (fileSplit.length == 2 && fileToEnonicMapping.containsKey(fileSplit[1])) {
                logger.warn ("Henter " + fileToEnonicMapping.get(fileSplit[1]).get(fileSplit[0]) + " på nytt");
                try {
                    Content<Innholdstekst> content = contentRetriever.getContent(new URI(fileToEnonicMapping.get(fileSplit[1]).get(fileSplit[0])));
                    Map<String, Innholdstekst> innhold = content.toMap(Innholdstekst.KEY);
                    for (Map.Entry<String, Innholdstekst> entry : innhold.entrySet()) {
                        holder.getProperties().put(entry.getValue().key, spripPTag(entry.getValue().value));
                    }
                } catch (Exception e) {
                    logger.warn("Feilet under uthenting av: " + fileToEnonicMapping.get(fileSplit[1]).get(fileSplit[0]) + ": " + e, e);
                    return holder;
                }
            }
        }
        return holder;
    }


    private String spripPTag(String value) {
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

}
