package no.nav.sbl.dialogarena.service;

import java.util.Collections;
import java.util.List;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;

public final class HandlebarContext {
    
    private final JsonInternalSoknad internalSoknad;
    private final boolean utvidetSoknad;

    public HandlebarContext(JsonInternalSoknad internalSoknad, boolean utvidetSoknad) {
        this.internalSoknad = internalSoknad;
        this.utvidetSoknad = utvidetSoknad;
    }
    
    public JsonSoknad getSoknad() {
        return internalSoknad.getSoknad();
    }
    
    public JsonVedleggSpesifikasjon getJsonVedleggSpesifikasjon() {
        return internalSoknad.getVedlegg();
    }
    
    public JsonSoknadsmottaker getMottaker() {
        return internalSoknad.getMottaker();
    }

    public boolean getUtvidetSoknad() {
        return utvidetSoknad;
    }
}
