package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import java.io.IOException;


public interface HtmlGenerator {
    String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException;
}
