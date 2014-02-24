package no.nav.sbl.dialogarena.print;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.IOException;


public interface HtmlGenerator {
    public String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException;
}
