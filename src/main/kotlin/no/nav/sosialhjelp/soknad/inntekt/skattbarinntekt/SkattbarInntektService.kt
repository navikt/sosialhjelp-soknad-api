package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.getForskuddstrekk
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.grupperOgSummerEtterUtbetalingsStartDato
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.mapToUtbetalinger
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SkattbarInntektService(
    private val skatteetatenClient: SkatteetatenClient
) {

    fun hentUtbetalinger(fnummer: String): List<Utbetaling>? {
        val skattbarInntekt = skatteetatenClient.hentSkattbarinntekt(fnummer)
        val utbetalinger = skattbarInntekt.mapToUtbetalinger()
        val forskuddstrekk = skattbarInntekt.getForskuddstrekk()
        val summerteUtbetalinger = summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSamletUtbetaling(utbetalinger, forskuddstrekk)
        return filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(summerteUtbetalinger)
    }

    private fun summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSamletUtbetaling(utbetalinger: List<Utbetaling>?, trekk: List<Utbetaling>): List<Utbetaling>? {
        val bruttoOrgPerMaaned = utbetalinger?.groupBy { it.orgnummer }?.let { getUtBetalingPerMaanedPerOrg(it) }
        val trekkOrgPerMaaned = getUtBetalingPerMaanedPerOrg(trekk.groupBy { it.orgnummer })
        val utbetalingerBrutto: List<Utbetaling>? = bruttoOrgPerMaaned?.values?.flatMap { it.values }

        return utbetalingerBrutto
            ?.filter { it.orgnummer != "995277670" } // NAV ØKONOMILINJEN
            ?.onEach {
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
        return utbetalinger
            ?.groupBy { it.orgnummer }
            ?.values
            ?.map {
                val nyesteDato: LocalDate = it.maxOf { utbetaling -> utbetaling.periodeFom }
                grupperOgSummerEtterUtbetalingsStartDato(it)[nyesteDato] ?: throw SosialhjelpSoknadApiException("Fant ingen utbetalinger for nyeste dato")
            }
    }
}
