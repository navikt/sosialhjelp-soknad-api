package no.nav.sbl.dialogarena.service;

import java.util.List;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;

public final class HandlebarContext {
    
    private final JsonSoknad soknad;
    private List<Vedlegg> vedleggListe;

    
    public HandlebarContext(JsonSoknad soknad, List<Vedlegg> vedleggListe) {
        this.soknad = soknad;
        this.vedleggListe = vedleggListe;
    }
    
    
    public JsonSoknad getSoknad() {
        return soknad;
    }
    
    public List<Vedlegg> getVedleggListe() {
        return vedleggListe;
    }
}
