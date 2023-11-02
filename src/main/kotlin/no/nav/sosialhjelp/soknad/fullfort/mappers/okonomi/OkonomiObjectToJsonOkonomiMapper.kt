package no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.domene.okonomi.OkonomiBubbleObject
import no.nav.sosialhjelp.soknad.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.domene.okonomi.type.FormueType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.InntektType
import no.nav.sosialhjelp.soknad.domene.okonomi.type.UtgiftType


fun JsonOkonomi.initChildren() {
    if (opplysninger == null) withOpplysninger(JsonOkonomiopplysninger())
    if (oversikt == null) withOversikt(JsonOkonomioversikt())
}

fun JsonOkonomi.mapFromDomainObject(domain: OkonomiBubbleObject) {
    initChildren()

    when (domain.type) {
        InntektType.DOKUMENTASJON_FORSIKRINGSUTBETALING,
        InntektType.DOKUMENTASJON_ANNET_INNTEKTER,
        InntektType.DOKUMENTASJON_UTBYTTE,
        InntektType.SALGSOPPGJOR_EIENDOM,
        InntektType.SLUTTOPPGJOR_ARBEID,
        InntektType.HUSBANKEN_VEDTAK
        -> with (domain as Inntekt) { opplysninger.utbetaling.add(this.toJsonOkonomiOpplysningUtbetaling()) }

        InntektType.BARNEBIDRAG_MOTTAR,
        InntektType.LONNSLIPP_ARBEID,
        InntektType.STUDENT_VEDTAK
        -> with (domain as Inntekt) { oversikt.inntekt.add(this.toJsonOkonomioversiktInntekt()) }

        UtgiftType.DOKUMENTASJON_ANNET_BOUTGIFT,
        UtgiftType.FAKTURA_ANNET_BARNUTGIFT,
        UtgiftType.FAKTURA_TANNBEHANDLING,
        UtgiftType.FAKTURA_KOMMUNALEAVGIFTER,
        UtgiftType.FAKTURA_FRITIDSAKTIVITET,
        UtgiftType.FAKTURA_OPPVARMING,
        UtgiftType.FAKTURA_STROM,
        UtgiftType.ANDRE_UTGIFTER
        -> with (domain as Utgift) { opplysninger.utgift.add(this.toJsonOkonomiopplysningUtgift()) }

        UtgiftType.BARNEBIDRAG_BETALER,
        UtgiftType.FAKTURA_BARNEHAGE,
        UtgiftType.FAKTURA_SFO,
        UtgiftType.FAKTURA_HUSLEIE,
        UtgiftType.NEDBETALINGSPLAN_AVDRAGSLAN
        -> with (domain as Utgift) { oversikt.utgift.add(this.toJsonOkonomioversiktUtgift()) }

        FormueType.KONTOOVERSIKT_AKSJER,
        FormueType.KONTOOVERSIKT_ANNET,
        FormueType.KONTOOVERSIKT_BRUKSKONTO,
        FormueType.KONTOOVERSIKT_BSU,
        FormueType.KONTOOVERSIKT_LIVSFORSIKRING,
        FormueType.KONTOOVERSIKT_SPAREKONTO
        -> with (domain as Formue) { oversikt.formue.add(this.toJsonOkonomioversiktFormue()) }

        else -> throw IllegalStateException("OkonomiTypen mangler mapping til Json-objekt")
    }
}
