package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;

import static java.util.Collections.emptyList;

public class UtgifterOgGjeldSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var utgifter = jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtgift();

        return new Steg.Builder()
                .withStegNr(7)
                .withTittel("utgifterbolk.tittel")
                .withAvsnitt(emptyList())
                .withErFerdigUtfylt(true)
                .build();
    }

}
