package no.nav.sbl.dialogarena.service;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;

public final class HandlebarContext {
    
    private final JsonSoknad soknad;

    
    public HandlebarContext(JsonSoknad soknad) {
        this.soknad = soknad;
    }
    
    
    public JsonSoknad getSoknad() {
        return soknad;
    }
}
