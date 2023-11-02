package no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetalingKomponent
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.domene.Kilde
import no.nav.sosialhjelp.soknad.domene.Kilde.*
import no.nav.sosialhjelp.soknad.domene.okonomi.BeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.domene.okonomi.Bostotte
import no.nav.sosialhjelp.soknad.domene.okonomi.Formue
import no.nav.sosialhjelp.soknad.domene.okonomi.Inntekt
import no.nav.sosialhjelp.soknad.domene.okonomi.Komponent
import no.nav.sosialhjelp.soknad.domene.okonomi.Utgift
import no.nav.sosialhjelp.soknad.domene.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.domene.okonomi.Vedtaksstatus.*
import no.nav.sosialhjelp.soknad.fullfort.mappers.okonomi.type.toSoknadJsonType

fun Inntekt.toJsonOkonomioversiktInntekt(): JsonOkonomioversiktInntekt {
    return JsonOkonomioversiktInntekt()
        .withKilde(JsonKilde.BRUKER) // alltid bruker
        .withType(type.toSoknadJsonType())
        .withTittel(tittel)
        .withBrutto(brutto)
        .withNetto(netto)
}

fun Inntekt.toJsonOkonomiOpplysningUtbetaling(): JsonOkonomiOpplysningUtbetaling {
    if (this.utbetaling == null) throw IllegalArgumentException("Utbetaling er null for Inntekt med Utbetaling-type $type")
    return JsonOkonomiOpplysningUtbetaling()
        .withKilde(utbetaling.kilde.toJsonKilde())
        .withType(type.toSoknadJsonType())
        .withTittel(tittel)
        .withOrganisasjon(
            JsonOrganisasjon()
                .withNavn("")
                .withOrganisasjonsnummer(utbetaling.orgnummer)
        )
        .withBelop(utbetaling.belop)
        .withNetto(netto?.toDouble())
        .withBrutto(brutto?.toDouble())
        .withSkattetrekk(utbetaling.skattetrekk)
        .withAndreTrekk(utbetaling.andreTrekk)
        .withUtbetalingsdato(utbetaling.utbetalingsdato.toString())
        .withPeriodeFom(utbetaling.periodeStart.toString())
        .withPeriodeTom(utbetaling.periodeSlutt.toString())
        .withKomponenter(
            utbetaling.komponent?.map { it.toJsonOkonomiOpplysningUtbetalingKomponent() }
        )
}

fun Komponent.toJsonOkonomiOpplysningUtbetalingKomponent(): JsonOkonomiOpplysningUtbetalingKomponent {
    return JsonOkonomiOpplysningUtbetalingKomponent()
        .withType(type)
        .withBelop(belop)
        .withSatsType(satsType)
        .withSatsAntall(satsAntall)
        .withSatsBelop(satsBelop)
}

fun Utgift.toJsonOkonomiopplysningUtgift(): JsonOkonomiOpplysningUtgift {
    return JsonOkonomiOpplysningUtgift()
        .withKilde(JsonKilde.BRUKER) // alltid bruker
        .withType(type.toSoknadJsonType())
        .withTittel(tittel)
        .withBelop(belop)
}

fun Utgift.toJsonOkonomioversiktUtgift(): JsonOkonomioversiktUtgift {
    return JsonOkonomioversiktUtgift()
        .withKilde(JsonKilde.BRUKER) // alltid bruker
        .withType(type.toSoknadJsonType())
        .withTittel(tittel)
        .withBelop(belop)
}

fun Formue.toJsonOkonomioversiktFormue(): JsonOkonomioversiktFormue {
    return JsonOkonomioversiktFormue()
        .withKilde(JsonKilde.BRUKER) // alltid bruker
        .withType(type.toSoknadJsonType())
        .withTittel(tittel)
        .withBelop(belop)
}

fun Bostotte.toJsonBostotteSak(): JsonBostotteSak {
    return JsonBostotteSak()
        .withType(type)
        .withDato(dato.toString())
        .withStatus(status?.name)
        .withBeskrivelse(beskrivelse)
        .withVedtaksstatus(vedtaksstatus?.toJsonBostotteSak_Vedtaksstatus())
}

fun Vedtaksstatus.toJsonBostotteSak_Vedtaksstatus(): JsonBostotteSak.Vedtaksstatus {
    return when (this) {
        INNVILGET -> JsonBostotteSak.Vedtaksstatus.INNVILGET
        AVSLAG -> JsonBostotteSak.Vedtaksstatus.AVSLAG
        AVVIST -> JsonBostotteSak.Vedtaksstatus.AVVIST
    }
}

fun BeskrivelserAvAnnet.toJsonOkonomibeskrivelserAvAnnet(): JsonOkonomibeskrivelserAvAnnet {
    return JsonOkonomibeskrivelserAvAnnet()
        .withVerdi(verdi)
        .withSparing(sparing)
        .withUtbetaling(utbetaling)
        .withBoutgifter(boutgifter)
        .withBarneutgifter(barneutgifter)
}

fun Kilde.toJsonKilde(): JsonKilde = if (this == BRUKER) JsonKilde.BRUKER else JsonKilde.SYSTEM

