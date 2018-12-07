package no.nav.sbl.sosialhjelp.pdf;


import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;

import java.io.IOException;


public interface HtmlGenerator {
    String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad) throws IOException;

    String genererHtmlForPdf(JsonInternalSoknad internalSoknad, boolean utvidetSoknad) throws IOException;

    String genererHtmlForPdf(JsonInternalSoknad internalSoknad, String file, boolean erEttersending) throws IOException;
}

