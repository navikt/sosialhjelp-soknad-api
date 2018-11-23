package no.nav.sbl.dialogarena.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.sbl.dialogarena.service.context.InntektEllerUtgiftType;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;

public final class HandlebarContext {
    
    public static final Locale SPRAK = new Locale("nb", "NO");
    private final JsonInternalSoknad internalSoknad;
    private final boolean utvidetSoknad;
    private final boolean erEttersending;

    public HandlebarContext(JsonInternalSoknad internalSoknad, boolean utvidetSoknad, boolean erEttersending) {
        this.internalSoknad = internalSoknad;
        this.utvidetSoknad = utvidetSoknad;
        this.erEttersending = erEttersending;
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
    
    public boolean getErEttersending() {
        return erEttersending;
    }

    public Collection<InntektEllerUtgiftType> getFormuetyper() {
        final List<JsonOkonomioversiktFormue> formue = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getFormue();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .distinct()
            .collect(Collectors.toSet());
    }
    
    public Collection<InntektEllerUtgiftType> getUtbetalingstyper() {
        final List<JsonOkonomiOpplysningUtbetaling> formue = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .distinct()
            .collect(Collectors.toSet());
    }
    
    public Collection<InntektEllerUtgiftType> getOpplysningUtgiftstyper() {
        final List<JsonOkonomiOpplysningUtgift> formue = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtgift();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .distinct()
            .collect(Collectors.toSet());
    }
    
    public Collection<InntektEllerUtgiftType> getOversiktUtgiftstyper() {
        final List<JsonOkonomioversiktUtgift> formue = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getUtgift();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .distinct()
            .collect(Collectors.toSet());
    }
    
}
