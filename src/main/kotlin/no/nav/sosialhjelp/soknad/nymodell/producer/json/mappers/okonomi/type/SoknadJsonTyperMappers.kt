package no.nav.sosialhjelp.soknad.nymodell.producer.json.mappers.okonomi.type

import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.SamtykkeType
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.SamtykkeType.BOSTOTTE_SAMTYKKE
import no.nav.sosialhjelp.soknad.nymodell.domene.brukerdata.SamtykkeType.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.BEKREFTELSE_BARNEUTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.BEKREFTELSE_BOUTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.BEKREFTELSE_SPARING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.BEKREFTELSE_UTBETALING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.BEKREFTELSE_VERDI
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.BOSTOTTE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.BekreftelseType.STUDIELAN
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_AKSJER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_ANNET
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_BRUKSKONTO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_BSU
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_LIVSFORSIKRING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.FormueType.KONTOOVERSIKT_SPAREKONTO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.InntektType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.ANDRE_UTGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.BARNEBIDRAG_BETALER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.DOKUMENTASJON_ANNET_BOUTGIFT
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_ANNET_BARNUTGIFT
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_BARNEHAGE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_FRITIDSAKTIVITET
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_HUSLEIE
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_KOMMUNALEAVGIFTER
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_OPPVARMING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_SFO
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_STROM
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.FAKTURA_TANNBEHANDLING
import no.nav.sosialhjelp.soknad.nymodell.domene.okonomi.UtgiftType.NEDBETALINGSPLAN_AVDRAGSLAN

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

fun InntektType.toSoknadJsonType(): String {
    return when (this) {
        InntektType.BARNEBIDRAG_MOTTAR -> SoknadJsonTyper.BARNEBIDRAG
        InntektType.DOKUMENTASJON_ANNET_INNTEKTER -> SoknadJsonTyper.UTBETALING_ANNET
        InntektType.DOKUMENTASJON_FORSIKRINGSUTBETALING -> SoknadJsonTyper.UTBETALING_FORSIKRING
        InntektType.DOKUMENTASJON_UTBYTTE -> SoknadJsonTyper.UTBETALING_UTBYTTE
        InntektType.HUSBANKEN_VEDTAK -> SoknadJsonTyper.UTBETALING_HUSBANKEN
        InntektType.LONNSLIPP_ARBEID -> SoknadJsonTyper.JOBB
        InntektType.SALGSOPPGJOR_EIENDOM -> SoknadJsonTyper.UTBETALING_SALG
        InntektType.SLUTTOPPGJOR_ARBEID -> SoknadJsonTyper.SLUTTOPPGJOER
        InntektType.STUDENT_VEDTAK -> SoknadJsonTyper.STUDIELAN
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
