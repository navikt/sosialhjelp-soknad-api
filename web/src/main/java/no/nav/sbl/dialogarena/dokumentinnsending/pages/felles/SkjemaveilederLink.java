package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import org.apache.wicket.markup.html.link.ExternalLink;

public class SkjemaveilederLink extends ExternalLink {

    public SkjemaveilederLink(String id, String propertyValues) {
        super(id
                , getSystemProperty("dokumentinnsending.skjemaveileder.url") + propertyValues);
    }

    private static String getSystemProperty(String key) {
        return System.getProperty(key);
    }
}
