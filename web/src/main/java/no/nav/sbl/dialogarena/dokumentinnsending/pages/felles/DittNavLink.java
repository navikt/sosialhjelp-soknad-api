package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import org.apache.wicket.markup.html.link.ExternalLink;

public class DittNavLink extends ExternalLink {

    public DittNavLink(String id) {
        this(id, "dittnav");
    }

    public DittNavLink(String id, String lenkeTekstResource) {
        super(id, getSystemProperty("minehenvendelser.link.url") + lenkeTekstResource);
    }

    private static String getSystemProperty(String key) {
        return System.getProperty(key);
    }
}