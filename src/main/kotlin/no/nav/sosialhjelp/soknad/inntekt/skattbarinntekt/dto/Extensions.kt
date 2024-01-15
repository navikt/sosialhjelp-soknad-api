package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto

import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun SkattbarInntekt?.mapToUtbetalinger(): List<Utbetaling>? {
    if (this == null) {
        return null
    }

    val utbetalingerLonn: MutableList<Utbetaling> = mutableListOf()
    val utbetalingerPensjon: MutableList<Utbetaling> = mutableListOf()
    val dagmammaIEgenBolig: MutableList<Utbetaling> = mutableListOf()
    val lottOgPartInnenFiske: MutableList<Utbetaling> = mutableListOf()

    oppgaveInntektsmottaker
        .forEach { oppgaveInntektsmottaker ->
            val kalenderManed = YearMonth.parse(oppgaveInntektsmottaker.kalendermaaned, arManedFormatter)
            val fom = kalenderManed.atDay(1)
            val tom = kalenderManed.atEndOfMonth()

            oppgaveInntektsmottaker.inntekt
                .filter { it.inngaarIGrunnlagForTrekk == true }
                .forEach {
                    if (it.loennsinntekt != null) {
                        utbetalingerLonn.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, it, "Lønn"))
                    }
                    if (it.pensjonEllerTrygd != null) {
                        utbetalingerPensjon.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, it, "Pensjon"))
                    }
                    if (it.dagmammaIEgenBolig != null) {
                        dagmammaIEgenBolig.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, it, "Dagmamma i egen bolig"))
                    }
                    if (it.lottOgPartInnenFiske != null) {
                        lottOgPartInnenFiske.add(getUtbetaling(oppgaveInntektsmottaker, fom, tom, it, "Lott og part innen fiske"))
                    }
                }
        }

    val aggregertUtbetaling: MutableList<Utbetaling> = mutableListOf()
    aggregertUtbetaling.addAll(utbetalingerLonn)
    aggregertUtbetaling.addAll(utbetalingerPensjon)
    aggregertUtbetaling.addAll(dagmammaIEgenBolig)
    aggregertUtbetaling.addAll(lottOgPartInnenFiske)
    aggregertUtbetaling.addAll(getForskuddstrekk())

    return aggregertUtbetaling
}

fun SkattbarInntekt?.getForskuddstrekk(): List<Utbetaling> {
    if (this == null) {
        return emptyList()
    }
    val forskuddstrekk: MutableList<Utbetaling> = mutableListOf()
    oppgaveInntektsmottaker
        .forEach {
            val kalenderManed = YearMonth.parse(it.kalendermaaned, arManedFormatter)
            val fom = kalenderManed.atDay(1)
            val tom = kalenderManed.atEndOfMonth()
            it.forskuddstrekk
                .forEach { ft ->
                    val utbetaling = Utbetaling(
                        type = "skatteopplysninger",
                        brutto = 0.0,
                        skattetrekk = ft.beloep?.toDouble() ?: 0.0,
                        periodeFom = fom,
                        periodeTom = tom,
                        tittel = "Forskuddstrekk",
                        orgnummer = it.opplysningspliktigId,
                    )
                    forskuddstrekk.add(utbetaling)
                }
        }
    return forskuddstrekk
}

private fun getUtbetaling(
    oppgaveInntektsmottaker: OppgaveInntektsmottaker,
    fom: LocalDate,
    tom: LocalDate,
    inntekt: Inntekt,
    tittel: String,
): Utbetaling {
    return Utbetaling(
        type = "skatteopplysninger",
        brutto = inntekt.beloep?.toDouble() ?: 0.0,
        skattetrekk = 0.0,
        periodeFom = fom,
        periodeTom = tom,
        tittel = tittel,
        orgnummer = oppgaveInntektsmottaker.opplysningspliktigId,
    )
}

internal fun grupperOgSummerEtterUtbetalingsStartDato(utbetalinger: List<Utbetaling>): Map<LocalDate, Utbetaling> {
    val ret: MutableMap<LocalDate, Utbetaling> = HashMap()
    utbetalinger
        .groupBy { it.periodeFom }
        .forEach {
            it.value
                .reduce { utbetaling, utbetaling2 ->
                    utbetaling.brutto += utbetaling2.brutto
                    utbetaling.skattetrekk += utbetaling2.skattetrekk
                    utbetaling
                }
            ret[it.key] = it.value[0]
        }
    return ret
}

private val arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
