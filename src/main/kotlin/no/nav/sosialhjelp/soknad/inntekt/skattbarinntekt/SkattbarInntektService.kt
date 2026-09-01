package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.domain.Utbetaling
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.SkattbarInntekt
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.getForskuddstrekk
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.grupperOgSummerEtterUtbetalingsStartDato
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.dto.mapToUtbetalinger
import no.nav.sosialhjelp.soknad.v2.register.fetchers.SkatteetatenException
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SkattbarInntektService(
    private val skatteetatenClient: SkatteetatenClient,
) {
    fun hentInntekt(): List<Utbetaling>? {
        logger.info("Henter skattbar inntekt fra Skatteetaten")

        val skattbarInntekt =
            when (val response = skatteetatenClient.hentSkattbarinntekt()) {
                is SkattbarInntektResponse.Success -> response.inntekt
                is SkattbarInntektResponse.Error -> throw SkatteetatenException(response.error, response.cause)
                is SkattbarInntektResponse.NotFound -> {
                    logger.info("Fant ingen skattbar inntekt")
                    SkattbarInntekt()
                }
            }

        val utbetalinger = skattbarInntekt.mapToUtbetalinger()
        val forskuddstrekk = skattbarInntekt.getForskuddstrekk()
        val summerteUtbetalinger =
            summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSamletUtbetaling(
                utbetalinger,
                forskuddstrekk,
            )
        return filtrerUtbetalingerSlikAtViFaarSisteMaanedFraHverArbeidsgiver(summerteUtbetalinger)
    }

    private fun summerUtbetalingerPerMaanedPerOrganisasjonOgForskuddstrekkSamletUtbetaling(
        utbetalinger: List<Utbetaling>,
        trekk: List<Utbetaling>,
    ): List<Utbetaling> {
        val bruttoOrgPerMaaned = getUtBetalingPerMaanedPerOrg(utbetalinger.groupBy { it.orgnummer })
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
        return utbetalinger
            ?.groupBy { it.orgnummer }
            ?.values
            ?.map {
                val nyesteDato: LocalDate = it.maxOf { utbetaling -> utbetaling.periodeFom }
                grupperOgSummerEtterUtbetalingsStartDato(
                    it,
                )[nyesteDato] ?: throw SosialhjelpSoknadApiException("Fant ingen utbetalinger for nyeste dato")
            }
    }

    companion object {
        private val logger by logger()
    }
}
