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
        tittel: String? = null,
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
        isExpected: Boolean,
        tittel: String? = null,
    ) {
        if (!isExpected) {
            formuer.removeIf { it.type == type }
        } else if (formuer.any { it.type == type }) {
            return
        } else {
            check(tittel != null) { "tittel must be set when isExpected is true" }
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
        isExpected: Boolean,
        tittel: String? = null,
    ) {
        if (!isExpected) {
            inntekter.removeIf { it.type == type }
        } else if (inntekter.any { it.type == type }) {
            return
        } else {
            check(tittel != null) { "tittel must be set when isExpected is true" }

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
        isExpected: Boolean,
        tittel: String? = null,
    ) {
        if (!isExpected) {
            utgifter.removeIf { it.type == type }
        } else if (utgifter.any { it.type == type }) {
            return
        } else {
            check(tittel != null) { "tittel must be set when isExpected is true" }

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
        isExpected: Boolean,
        tittel: String? = null,
    ) {
        if (!isExpected) {
            utgifter.removeIf { it.type == type }
        } else if (utgifter.any { it.type == type }) {
            return
        } else {
            check(tittel != null) { "tittel must be set when isExpected is true" }

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
        isExpected: Boolean,
        tittel: String? = null,
    ) {
        if (!isExpected) {
            utbetalinger.removeIf { it.type == type }
        } else if (utbetalinger.any { it.type == type }) {
            return
        } else {
            check(tittel != null) { "tittel must be set when isExpected is true" }

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
