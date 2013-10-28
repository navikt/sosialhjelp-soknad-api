package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.modig.content.Content;
import no.nav.modig.content.ContentRetriever;
import no.nav.modig.content.enonic.innholdstekst.Innholdstekst;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import javax.inject.Inject;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static no.nav.modig.content.enonic.innholdstekst.Innholdstekst.KEY;


public class NavMessageSource extends ReloadableResourceBundleMessageSource {


    @Inject
    private ContentRetriever contentRetriever;

    private Map<String, Map<String, String>> fileToEnonicMapping;
    private boolean enableEnonic;

    public void setEnonicMap(Map<String, Map<String, String>> enonicMap) {
        this.fileToEnonicMapping = enonicMap;
    }

    public void setEnableEnonic(boolean enable) {
        enableEnonic = enable;
    }


    public Properties getBundleFor(String prefix, Locale locale) {
        PropertiesHolder mergedProperties = getMergedProperties(locale);
        return mergedProperties.getProperties();
    }

    @Override
    protected PropertiesHolder refreshProperties(String filename, PropertiesHolder propHolder) {

        PropertiesHolder holder = super.refreshProperties(filename, propHolder);
        if (enableEnonic) {
            String[] fileSplit = filename.split("_");
            if (fileSplit.length > 1 && fileToEnonicMapping.containsKey(fileSplit[1])) {
                try {
                    Content<Innholdstekst> content = contentRetriever.getContent(new URI(fileToEnonicMapping.get(fileSplit[1]).get(fileSplit[0])));
                    Map<String, Innholdstekst> innhold = content.toMap(KEY);
                    for (Map.Entry<String, Innholdstekst> entry : innhold.entrySet()) {
                        holder.getProperties().put(entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    return holder;
                }
            }
        }
        return holder;
    }
}
