package no.nav.sosialhjelp.soknad.skattbarinntekt

import no.nav.sosialhjelp.soknad.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.getForskuddstrekk
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.grupperOgSummerEtterUtbetalingsStartDato
import no.nav.sosialhjelp.soknad.skattbarinntekt.dto.mapToUtbetalinger
import java.time.LocalDate
import java.time.format.DateTimeFormatter

open class SkattbarInntektService(
    private val skatteetatenClient: SkatteetatenClient
) {

    open fun hentUtbetalinger(fnummer: String): List<Utbetaling>? {
        val skattbarInntekt = skatteetatenClient.hentSkattbarinntekt(fnummer)
        val utbetalinger = skattbarInntekt.mapToUtbetalinger()
        val forskuddstrekk = skattbarInntekt.getForskuddstrekk()
        val summerteUtbetalinger = summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSamletUtbetaling(utbetalinger, forskuddstrekk)
        return filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(summerteUtbetalinger)
    }

    private fun summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSamletUtbetaling(utbetalinger: List<Utbetaling>?, trekk: List<Utbetaling>): List<Utbetaling> {
        val bruttoOrgPerMaaned = getUtBetalingPerMaanedPerOrg(utbetalinger?.groupBy { it.orgnummer } ?: emptyMap())
        val trekkOrgPerMaaned = getUtBetalingPerMaanedPerOrg(trekk.groupBy { it.orgnummer })
        val utbetalingerBrutto: List<Utbetaling> = bruttoOrgPerMaaned.values.flatMap { it.values }

        return utbetalingerBrutto
            .filter { it.orgnummer != "995277670" } // NAV ØKONOMILINJEN
            .onEach {
                val localDateUtbetalingMap = trekkOrgPerMaaned[it.orgnummer]
                if (localDateUtbetalingMap != null) {
                    val trekkUtbetaling = localDateUtbetalingMap[it.periodeFom]
                    if (trekkUtbetaling != null) {
                        it.skattetrekk = trekkUtbetaling.skattetrekk
                    }
                }
                it.tittel = "Lønnsinntekt"
            }
    }

    private fun getUtBetalingPerMaanedPerOrg(orgUtbetaling: Map<String, List<Utbetaling>>): Map<String, Map<LocalDate, Utbetaling>> {
        val bruttoOrgPerMaaned: MutableMap<String, Map<LocalDate, Utbetaling>> = HashMap()
        orgUtbetaling.forEach {
            bruttoOrgPerMaaned[it.key] = grupperOgSummerEtterUtbetalingsStartDato(it.value)
        }
        return bruttoOrgPerMaaned
    }

    private fun filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(utbetalinger: List<Utbetaling>?): List<Utbetaling>? {
        if (utbetalinger == null) {
            return null
        }
        return utbetalinger
            .groupBy { it.orgnummer }.values
            .map {
                val nyesteDato: LocalDate = it.maxOf { utbetaling -> utbetaling.periodeFom!! }
                grupperOgSummerEtterUtbetalingsStartDato(it)[nyesteDato]!!
            }
    }

    companion object {
        private val arManedFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}
