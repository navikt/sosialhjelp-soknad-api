package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;

import java.util.List;

public final class StegUtils {

    private StegUtils() {
        // no-op
    }

    public static Avsnitt createAvsnitt(String tittel, List<Sporsmal> sporsmal) {
        return new Avsnitt.Builder()
                .withTittel(tittel)
                .withSporsmal(sporsmal)
                .build();
    }

}
