package no.nav.sosialhjelp.soknad.app.mapper

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift

object OkonomiMapper {
    fun setBekreftelse(
        opplysninger: JsonOkonomiopplysninger,
        type: String,
        verdi: Boolean?,
        tittel: String,
    ) {
        opplysninger.bekreftelse = opplysninger.bekreftelse ?: ArrayList()

        opplysninger.bekreftelse.firstOrNull { it.type == type }?.apply {
            withKilde(JsonKilde.BRUKER).withVerdi(verdi)
        } ?: opplysninger.bekreftelse.add(
            JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(type)
                .withTittel(tittel)
                .withVerdi(verdi),
        )
    }

    fun removeBekreftelserIfPresent(
        opplysninger: JsonOkonomiopplysninger,
        type: String?,
    ) {
        opplysninger.bekreftelse.removeIf { it.type.equals(type, ignoreCase = true) }
    }

    fun setFormueInOversikt(
        formuer: MutableList<JsonOkonomioversiktFormue>,
        type: String,
        tittel: String,
        isExpected: Boolean,
    ) {
        if (!isExpected) {
            formuer.removeIf { it.type == type }
        } else if (formuer.any { it.type == type }) {
            return
        } else {
            formuer.add(
                JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false),
            )
        }
    }

    fun setInntektInOversikt(
        inntekter: MutableList<JsonOkonomioversiktInntekt>,
        type: String,
        tittel: String,
        isExpected: Boolean,
    ) {
        if (!isExpected) {
            inntekter.removeIf { it.type == type }
        } else if (inntekter.any { it.type == type }) {
            return
        } else {
            inntekter.add(
                JsonOkonomioversiktInntekt()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false),
            )
        }
    }

    fun setUtgiftInOversikt(
        utgifter: MutableList<JsonOkonomioversiktUtgift>,
        type: String,
        tittel: String,
        isExpected: Boolean,
    ) {
        if (!isExpected) {
            utgifter.removeIf { it.type == type }
        } else if (utgifter.any { it.type == type }) {
            return
        } else {
            utgifter.add(
                JsonOkonomioversiktUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false),
            )
        }
    }

    fun setUtgiftInOpplysninger(
        utgifter: MutableList<JsonOkonomiOpplysningUtgift>,
        type: String,
        tittel: String,
        isExpected: Boolean,
    ) {
        if (!isExpected) {
            utgifter.removeIf { it.type == type }
        } else if (utgifter.any { it.type == type }) {
            return
        } else {
            utgifter.add(
                JsonOkonomiOpplysningUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false),
            )
        }
    }

    fun setUtbetalingInOpplysninger(
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        type: String,
        tittel: String,
        isExpected: Boolean,
    ) {
        if (!isExpected) {
            utbetalinger.removeIf { it.type == type }
        } else if (utbetalinger.any { it.type == type }) {
            return
        } else {
            utbetalinger.add(
                JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false),
            )
        }
    }
}
