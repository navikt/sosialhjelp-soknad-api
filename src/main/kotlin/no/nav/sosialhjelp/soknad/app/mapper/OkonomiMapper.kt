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

    fun setBekreftelse(opplysninger: JsonOkonomiopplysninger, type: String, verdi: Boolean?, tittel: String?) {
        if (opplysninger.bekreftelse == null) {
            opplysninger.bekreftelse = ArrayList()
        }
        
        val utbetaltBekreftelse = opplysninger.bekreftelse.firstOrNull { it.type == type }

        if (utbetaltBekreftelse != null) {
            utbetaltBekreftelse.withKilde(JsonKilde.BRUKER).withVerdi(verdi)
        } else {
            val bekreftelser = opplysninger.bekreftelse
            bekreftelser.add(
                JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withVerdi(verdi)
            )
        }
    }

    private fun addFormueIfNotPresentInOversikt(
        formuer: MutableList<JsonOkonomioversiktFormue>,
        type: String,
        tittel: String?
    ) {
        val jsonFormue = formuer.firstOrNull { it.type == type }
        if (jsonFormue == null) {
            formuer.add(
                JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false)
            )
        }
    }

    fun addInntektIfNotPresentInOversikt(
        inntekter: MutableList<JsonOkonomioversiktInntekt>,
        type: String,
        tittel: String?
    ) {
        val jsonInntekt = inntekter.firstOrNull { it.type == type }
        if (jsonInntekt == null) {
            inntekter.add(
                JsonOkonomioversiktInntekt()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false)
            )
        }
    }

    fun addUtgiftIfNotPresentInOversikt(
        utgifter: MutableList<JsonOkonomioversiktUtgift>,
        type: String,
        tittel: String?
    ) {
        val jsonUtgift = utgifter.firstOrNull { it.type == type }
        if (jsonUtgift == null) {
            utgifter.add(
                JsonOkonomioversiktUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false)
            )
        }
    }

    fun addUtgiftIfNotPresentInOpplysninger(
        utgifter: MutableList<JsonOkonomiOpplysningUtgift>,
        type: String?,
        tittel: String?
    ) {
        val jsonUtgift = utgifter.firstOrNull { it.type == type }
        if (jsonUtgift == null) {
            utgifter.add(
                JsonOkonomiOpplysningUtgift()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false)
            )
        }
    }

    fun addUtbetalingIfNotPresentInOpplysninger(
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        type: String,
        tittel: String?
    ) {
        val jsonUtbetaling = utbetalinger.firstOrNull { it.type == type }
        if (jsonUtbetaling == null) {
            utbetalinger.add(
                JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(type)
                    .withTittel(tittel)
                    .withOverstyrtAvBruker(false)
            )
        }
    }

    private fun removeFormueIfPresentInOversikt(formuer: MutableList<JsonOkonomioversiktFormue>, type: String) {
        formuer.removeIf { it.type == type }
    }

    fun removeInntektIfPresentInOversikt(inntekter: MutableList<JsonOkonomioversiktInntekt>, type: String) {
        inntekter.removeIf { it.type == type }
    }

    fun removeUtgiftIfPresentInOversikt(utgifter: MutableList<JsonOkonomioversiktUtgift>, type: String) {
        utgifter.removeIf { it.type == type }
    }

    fun removeUtgiftIfPresentInOpplysninger(utgifter: MutableList<JsonOkonomiOpplysningUtgift>, type: String?) {
        utgifter.removeIf { it.type == type }
    }

    fun removeUtbetalingIfPresentInOpplysninger(
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        type: String
    ) {
        utbetalinger.removeIf { it.type == type }
    }

    fun removeBekreftelserIfPresent(opplysninger: JsonOkonomiopplysninger, type: String?) {
        opplysninger.bekreftelse.removeIf { it.type.equals(type, ignoreCase = true) }
    }

    fun addFormueIfCheckedElseDeleteInOversikt(
        formuer: MutableList<JsonOkonomioversiktFormue>,
        type: String,
        tittel: String?,
        isChecked: Boolean
    ) {
        if (isChecked) {
            addFormueIfNotPresentInOversikt(formuer, type, tittel)
        } else {
            removeFormueIfPresentInOversikt(formuer, type)
        }
    }

    fun addInntektIfCheckedElseDeleteInOversikt(
        inntekter: MutableList<JsonOkonomioversiktInntekt>,
        type: String,
        tittel: String?,
        isChecked: Boolean
    ) {
        if (isChecked) {
            addInntektIfNotPresentInOversikt(inntekter, type, tittel)
        } else {
            removeInntektIfPresentInOversikt(inntekter, type)
        }
    }

    fun addutgiftIfCheckedElseDeleteInOversikt(
        utgifter: MutableList<JsonOkonomioversiktUtgift>,
        type: String,
        tittel: String?,
        isChecked: Boolean
    ) {
        if (isChecked) {
            addUtgiftIfNotPresentInOversikt(utgifter, type, tittel)
        } else {
            removeUtgiftIfPresentInOversikt(utgifter, type)
        }
    }

    fun addutgiftIfCheckedElseDeleteInOpplysninger(
        utgifter: MutableList<JsonOkonomiOpplysningUtgift>,
        type: String,
        tittel: String?,
        isChecked: Boolean
    ) {
        if (isChecked) {
            addUtgiftIfNotPresentInOpplysninger(utgifter, type, tittel)
        } else {
            removeUtgiftIfPresentInOpplysninger(utgifter, type)
        }
    }

    fun addUtbetalingIfCheckedElseDeleteInOpplysninger(
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        type: String,
        tittel: String?,
        isChecked: Boolean
    ) {
        if (isChecked) {
            addUtbetalingIfNotPresentInOpplysninger(utbetalinger, type, tittel)
        } else {
            removeUtbetalingIfPresentInOpplysninger(utbetalinger, type)
        }
    }
}
