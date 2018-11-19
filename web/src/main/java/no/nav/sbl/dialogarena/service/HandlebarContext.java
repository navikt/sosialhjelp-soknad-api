package no.nav.sbl.dialogarena.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.sbl.dialogarena.service.context.Formuetype;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
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

    public Collection<Formuetype> getFormuetyper() {
        final List<JsonOkonomioversiktFormue> formue = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getFormue();
        return formue.stream()
            .map((f) -> new Formuetype(f.getType(), f.getTittel()))
            .distinct()
            .collect(Collectors.toSet());
    }
}
