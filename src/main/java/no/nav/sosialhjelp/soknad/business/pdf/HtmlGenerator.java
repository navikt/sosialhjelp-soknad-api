package no.nav.sosialhjelp.soknad.business.pdf;


import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;

import java.io.IOException;


public interface HtmlGenerator {

    String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, boolean utvidetSoknad) throws IOException;

    String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, String file, boolean erEttersending, String eier) throws IOException;
}

