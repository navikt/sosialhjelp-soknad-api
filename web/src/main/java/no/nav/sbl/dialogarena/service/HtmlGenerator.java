package no.nav.sbl.dialogarena.service;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.io.IOException;


public interface HtmlGenerator {
    String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException;

    String fyllHtmlMalMedInnhold(WebSoknad soknad) throws IOException;
}
