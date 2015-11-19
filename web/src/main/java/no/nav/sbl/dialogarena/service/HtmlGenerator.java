package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;

import java.io.IOException;


public interface HtmlGenerator {
    String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException;

    String fyllHtmlMalMedInnholdNew(WebSoknad soknad, SoknadStruktur soknadStruktur, String file) throws IOException;
}
