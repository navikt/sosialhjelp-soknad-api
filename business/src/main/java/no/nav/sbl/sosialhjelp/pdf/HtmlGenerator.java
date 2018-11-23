package no.nav.sbl.sosialhjelp.pdf;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;

import java.io.IOException;


public interface HtmlGenerator {
    String fyllHtmlMalMedInnhold(WebSoknad soknad, String file) throws IOException;

    String fyllHtmlMalMedInnhold(WebSoknad soknad) throws IOException;

    String fyllHtmlMalMedInnhold(WebSoknad soknad, boolean utvidetSoknad) throws IOException;
    
    String genererHtmlForPdf(WebSoknad soknad, boolean utvidetSoknad) throws IOException;

    String genererHtmlForPdf(JsonInternalSoknad internalSoknad, boolean utvidetSoknad) throws IOException;

    String genererHtmlForPdf(WebSoknad soknad, String file, boolean erEttersending) throws IOException;

    String genererHtmlForPdf(JsonInternalSoknad internalSoknad, String file, boolean erEttersending) throws IOException;
}

