package no.nav.sbl.sosialhjelp.pdf;


import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;

import java.io.IOException;


public interface HtmlGenerator {
    String fyllHtmlMalMedInnhold(JsonInternalSoknad jsonInternalSoknad, JsonAdresse midlertidigAdresse) throws IOException;

    String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, JsonAdresse midlertidigAdresse, boolean utvidetSoknad) throws IOException;

    String fyllHtmlMalMedInnhold(JsonInternalSoknad internalSoknad, String file, boolean erEttersending, String eier) throws IOException;
}

