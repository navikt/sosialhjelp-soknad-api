package no.nav.sbl.dialogarena.service;

import java.util.Collections;
import java.util.List;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;

public final class HandlebarContext {
    
    private final JsonInternalSoknad internalSoknad;

    public HandlebarContext(JsonInternalSoknad internalSoknad) {
        this.internalSoknad = internalSoknad;
    }
    
    public JsonSoknad getSoknad() {
        return internalSoknad.getSoknad();
    }
    
    public JsonVedleggSpesifikasjon getJsonVedleggSpesifikasjon() {
        return internalSoknad.getVedlegg();
    }
}
