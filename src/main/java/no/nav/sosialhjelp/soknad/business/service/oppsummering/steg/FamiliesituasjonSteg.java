package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;

import static java.util.Collections.emptyList;

public class FamiliesituasjonSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        // todo implement
        return new Steg.Builder()
                .withStegNr(4)
                .withTittel("familiebolk.tittel")
                .withAvsnitt(emptyList())
                .build();
    }

}
