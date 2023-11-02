package no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi.type

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.domene.okonomi.type.BekreftelseType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.BekreftelseType.*
import no.nav.sosialhjelp.soknad.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.FormueType.*
import no.nav.sosialhjelp.soknad.domene.okonomi.type.GenerellOkonomiType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.GenerellOkonomiType.*
import no.nav.sosialhjelp.soknad.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.InntektType.*
import no.nav.sosialhjelp.soknad.domene.okonomi.type.SamtykkeType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.SamtykkeType.*
import no.nav.sosialhjelp.soknad.domene.okonomi.type.UtgiftType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.UtgiftType.*

fun BekreftelseType.toSoknadJsonType(): String  {
    return when(this) {
        BEKREFTELSE_BARNEUTGIFTER -> SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER
        BEKREFTELSE_BOUTGIFTER -> SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER
        BEKREFTELSE_SPARING -> SoknadJsonTyper.BEKREFTELSE_SPARING
        BEKREFTELSE_UTBETALING -> SoknadJsonTyper.BEKREFTELSE_UTBETALING
        BEKREFTELSE_VERDI -> SoknadJsonTyper.BEKREFTELSE_VERDI
        BOSTOTTE -> SoknadJsonTyper.BOSTOTTE
        STUDIELAN -> SoknadJsonTyper.STUDIELAN
    }
}

fun SamtykkeType.toSoknadJsonType(): String {
    return when(this) {
        BOSTOTTE_SAMTYKKE -> SoknadJsonTyper.BOSTOTTE_SAMTYKKE
        UTBETALING_SKATTEETATEN_SAMTYKKE -> SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
    }
}

fun InntektType.toSoknadJsonType(): String {
    return when (this) {
        BARNEBIDRAG_MOTTAR -> SoknadJsonTyper.BARNEBIDRAG
        DOKUMENTASJON_ANNET_INNTEKTER -> SoknadJsonTyper.UTBETALING_ANNET
        DOKUMENTASJON_FORSIKRINGSUTBETALING -> SoknadJsonTyper.UTBETALING_FORSIKRING
        DOKUMENTASJON_UTBYTTE -> SoknadJsonTyper.UTBETALING_UTBYTTE
        HUSBANKEN_VEDTAK -> SoknadJsonTyper.UTBETALING_HUSBANKEN
        LONNSLIPP_ARBEID -> SoknadJsonTyper.JOBB
        SALGSOPPGJOR_EIENDOM -> SoknadJsonTyper.UTBETALING_SALG
        SLUTTOPPGJOR_ARBEID -> SoknadJsonTyper.SLUTTOPPGJOER
        STUDENT_VEDTAK -> SoknadJsonTyper.STUDIELAN
    }
}

fun UtgiftType.toSoknadJsonType(): String {
    return when (this) {
        ANDRE_UTGIFTER -> SoknadJsonTyper.UTGIFTER_ANDRE_UTGIFTER
        BARNEBIDRAG_BETALER -> SoknadJsonTyper.BARNEBIDRAG
        DOKUMENTASJON_ANNET_BOUTGIFT -> SoknadJsonTyper.UTGIFTER_ANNET_BO
        FAKTURA_ANNET_BARNUTGIFT -> SoknadJsonTyper.UTGIFTER_ANNET_BARN
        FAKTURA_BARNEHAGE -> SoknadJsonTyper.UTGIFTER_BARNEHAGE
        FAKTURA_FRITIDSAKTIVITET -> SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
        FAKTURA_HUSLEIE -> SoknadJsonTyper.UTGIFTER_HUSLEIE
        FAKTURA_KOMMUNALEAVGIFTER -> SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
        FAKTURA_OPPVARMING -> SoknadJsonTyper.UTGIFTER_OPPVARMING
        FAKTURA_SFO -> SoknadJsonTyper.UTGIFTER_SFO
        FAKTURA_STROM -> SoknadJsonTyper.UTGIFTER_STROM
        FAKTURA_TANNBEHANDLING -> SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
        NEDBETALINGSPLAN_AVDRAGSLAN -> SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
    }
}


fun FormueType.toSoknadJsonType(): String {
    return when (this) {
        KONTOOVERSIKT_AKSJER -> SoknadJsonTyper.FORMUE_VERDIPAPIRER
        KONTOOVERSIKT_ANNET -> SoknadJsonTyper.FORMUE_ANNET
        KONTOOVERSIKT_BRUKSKONTO -> SoknadJsonTyper.FORMUE_BRUKSKONTO
        KONTOOVERSIKT_BSU -> SoknadJsonTyper.FORMUE_BSU
        KONTOOVERSIKT_LIVSFORSIKRING -> SoknadJsonTyper.FORMUE_LIVSFORSIKRING
        KONTOOVERSIKT_SPAREKONTO -> SoknadJsonTyper.FORMUE_SPAREKONTO
    }
}
