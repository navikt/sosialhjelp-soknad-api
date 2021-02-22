package no.nav.sosialhjelp.soknad.business.pdf;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.pdf.context.InntektEllerUtgiftType;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class HandlebarContext {
    
    public static final Locale SPRAK = new Locale("nb", "NO");
    private final JsonInternalSoknad internalSoknad;
    private final boolean utvidetSoknad;
    private final boolean erEttersending;
    private final String eier;

    public HandlebarContext(JsonInternalSoknad internalSoknad, boolean utvidetSoknad, boolean erEttersending, String eier) {
        this.internalSoknad = internalSoknad;
        this.utvidetSoknad = utvidetSoknad;
        this.erEttersending = erEttersending;
        this.eier = eier;
    }
    
    public JsonSoknad getSoknad() {
        return internalSoknad.getSoknad();
    }

    public JsonVedleggSpesifikasjon getJsonVedleggSpesifikasjon() {
        return internalSoknad.getVedlegg();
    }
    
    public String getNavEnhetsnavn() {
        JsonSoknadsmottaker mottaker = internalSoknad.getMottaker();
        if (mottaker != null) {
            return mottaker.getNavEnhetsnavn();
        } else {
            return internalSoknad.getSoknad().getMottaker().getNavEnhetsnavn();
        }
    }

    public JsonAdresse getMidlertidigAdresse() {
        return internalSoknad.getMidlertidigAdresse();
    }

    public boolean getUtvidetSoknad() {
        return utvidetSoknad;
    }
    
    public boolean getErEttersending() {
        return erEttersending;
    }

    public boolean getHarSystemregistrerteBarn() {
        return internalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt().getAnsvar().stream()
                .anyMatch(ansvar -> ansvar.getBarn().getKilde().equals(JsonKilde.SYSTEM));
    }

    public long getAntallSystemregistrerteBarn() {
        return internalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt().getAnsvar().stream()
                .filter(ansvar -> ansvar.getBarn().getKilde().equals(JsonKilde.SYSTEM))
                .count();
    }

    public boolean getHarBrukerregistrerteBarn() {
        return internalSoknad.getSoknad().getData().getFamilie().getForsorgerplikt().getAnsvar().stream()
                .anyMatch(ansvar -> ansvar.getBarn().getKilde().equals(JsonKilde.BRUKER));
    }

    public Collection<InntektEllerUtgiftType> getFormuetyper() {
        final List<JsonOkonomioversiktFormue> formue = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getFormue();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .collect(Collectors.toSet());
    }
    
    public Collection<InntektEllerUtgiftType> getUtbetalingstyper() {
        final List<JsonOkonomiOpplysningUtbetaling> formue = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .collect(Collectors.toSet());
    }
    
    public Collection<InntektEllerUtgiftType> getOpplysningUtgiftstyper() {
        final List<JsonOkonomiOpplysningUtgift> formue = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtgift();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .collect(Collectors.toSet());
    }
    
    public Collection<InntektEllerUtgiftType> getOversiktUtgiftstyper() {
        final List<JsonOkonomioversiktUtgift> formue = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getUtgift();
        return formue.stream()
            .map((f) -> new InntektEllerUtgiftType(f.getType(), f.getTittel()))
            .collect(Collectors.toSet());
    }
    
}
