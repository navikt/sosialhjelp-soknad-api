package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;

import static java.util.Collections.emptyList;

public class BosituasjonSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        // todo implement

        return new Steg.Builder()
                .withStegNr(5)
                .withTittel("bosituasjonbolk.tittel")
                .withAvsnitt(emptyList())
                .build();
    }
}
