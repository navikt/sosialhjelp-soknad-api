package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType

// OpplysningType inneholder også typer som ikke skal ha dokumentasjon - frontend trenger en eksplisitt liste
enum class DokumentasjonType(val opplysningType: OpplysningType) {
    // UTGIFTER
    UTGIFTER_ANNET_BO(UtgiftType.UTGIFTER_ANNET_BO),
    UTGIFTER_KOMMUNAL_AVGIFT(UtgiftType.UTGIFTER_KOMMUNAL_AVGIFT),
    UTGIFTER_OPPVARMING(UtgiftType.UTGIFTER_OPPVARMING),
    UTGIFTER_STROM(UtgiftType.UTGIFTER_STROM),
    UTGIFTER_BARN_TANNREGULERING(UtgiftType.UTGIFTER_BARN_TANNREGULERING),
    UTGIFTER_BARN_FRITIDSAKTIVITETER(UtgiftType.UTGIFTER_BARN_FRITIDSAKTIVITETER),
    UTGIFTER_ANNET_BARN(UtgiftType.UTGIFTER_ANNET_BARN),
    UTGIFTER_ANDRE_UTGIFTER(UtgiftType.UTGIFTER_ANDRE_UTGIFTER),
    BARNEBIDRAG_BETALER(UtgiftType.BARNEBIDRAG_BETALER),
    UTGIFTER_SFO(UtgiftType.UTGIFTER_SFO),
    UTGIFTER_BARNEHAGE(UtgiftType.UTGIFTER_BARNEHAGE),
    UTGIFTER_HUSLEIE(UtgiftType.UTGIFTER_HUSLEIE),
    UTGIFTER_BOLIGLAN(UtgiftType.UTGIFTER_BOLIGLAN),
    UTGIFTER_BOLIGLAN_AVDRAG(UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG),

    // FORMUE
    FORMUE_BRUKSKONTO(FormueType.FORMUE_BRUKSKONTO),
    FORMUE_BSU(FormueType.FORMUE_BSU),
    FORMUE_LIVSFORSIKRING(FormueType.FORMUE_LIVSFORSIKRING),
    FORMUE_SPAREKONTO(FormueType.FORMUE_SPAREKONTO),
    FORMUE_VERDIPAPIRER(FormueType.FORMUE_VERDIPAPIRER),
    FORMUE_ANNET(FormueType.FORMUE_ANNET),

    // INNTEKTER
    BARNEBIDRAG_MOTTAR(InntektType.BARNEBIDRAG_MOTTAR),
    STUDIELAN_INNTEKT(InntektType.STUDIELAN_INNTEKT),
    JOBB(InntektType.JOBB),
    UTBETALING_FORSIKRING(InntektType.UTBETALING_FORSIKRING),
    UTBETALING_UTBYTTE(InntektType.UTBETALING_UTBYTTE),
    UTBETALING_SALG(InntektType.UTBETALING_SALG),
    UTBETALING_HUSBANKEN(InntektType.UTBETALING_HUSBANKEN),
    UTBETALING_ANNET(InntektType.UTBETALING_ANNET),
    SLUTTOPPGJOER(InntektType.SLUTTOPPGJOER),

    // ANNET
    SKATTEMELDING(AnnenDokumentasjonType.SKATTEMELDING),
    SAMVARSAVTALE(AnnenDokumentasjonType.SAMVARSAVTALE),
    OPPHOLDSTILLATELSE(AnnenDokumentasjonType.OPPHOLDSTILLATELSE),
    HUSLEIEKONTRAKT(AnnenDokumentasjonType.HUSLEIEKONTRAKT),
    HUSLEIEKONTRAKT_KOMMUNAL(AnnenDokumentasjonType.HUSLEIEKONTRAKT_KOMMUNAL),
    BEHOV(AnnenDokumentasjonType.BEHOV),
}

fun OpplysningType.toDokumentasjonType(): DokumentasjonType =
    DokumentasjonType.entries.find { it.opplysningType == this }
        ?: error("Typen (${this.name} har ingen forventet dokumentasjon")
