package no.nav.sosialhjelp.soknad.innsending

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class KortSoknadService(
    private val digisosApiService: DigisosApiService,
    private val clock: Clock,
    private val soknadService: SoknadService,
    private val unleash: Unleash,
) {
    private val log by logger()

    fun transitionToKort(soknadId: UUID) {
        soknadService.updateKortSoknad(soknadId, true)
    }

    fun transitionToStandard(soknadId: UUID) {
        soknadService.updateKortSoknad(soknadId, false)
    }

    fun isQualified(
        token: String,
        kommunenummer: String,
    ): Boolean =
        digisosApiService
            .getSoknaderForUser(token)
            // Viktig med asSequence() her, sånn at den avbryter henting av innsynsfil tidlig hvis den finner et treff i any()
            .asSequence()
            .filter { it.kommunenummer == kommunenummer }
            .sortedByDescending { it.sistEndret }
            .mapNotNull { soknad ->
                soknad.digisosSoker?.metadata?.let {
                    digisosApiService.getInnsynsfilForSoknad(soknad.fiksDigisosId, it, token)
                }
            }.any { innsynsfil ->
                innsynsfil.hasRecentSoknadFromFiks() || innsynsfil.hasRecentOrUpcomingUtbetalinger()
            }

    fun isEnabled(kommunenummer: String?): Boolean {
        val context = kommunenummer?.let { UnleashContext.builder().addProperty("kommunenummer", it).build() } ?: UnleashContext.builder().build()
        return unleash.isEnabled("sosialhjelp.soknad.kort_soknad", context, false)
    }

    private fun JsonDigisosSoker.hasRecentSoknadFromFiks(): Boolean {
        val mottattSiste120Dager =
            hendelser
                ?.asSequence()
                ?.filter { it is JsonSoknadsStatus && it.status == JsonSoknadsStatus.Status.MOTTATT }
                ?.mapNotNull { it.hendelsestidspunkt }
                ?.firstOrNull { it.toLocalDateTime() >= LocalDateTime.now(clock).minusDays(120) }
        if (mottattSiste120Dager != null) {
            log.info("Bruker kvaliserer til kort søknad via søknad mottatt $mottattSiste120Dager")
            return true
        }
        return false
    }

    private fun JsonDigisosSoker.hasRecentOrUpcomingUtbetalinger(): Boolean {
        val fourMonthsAgo = LocalDateTime.now(clock).minusDays(120)
        val in14Days = LocalDateTime.now(clock).plusDays(14)

        val utbetaltSiste120Dager =
            hendelser
                ?.asSequence()
                ?.filterIsInstance<JsonUtbetaling>()
                ?.filter { it.status == JsonUtbetaling.Status.UTBETALT && it.utbetalingsdato != null }
                ?.map { it.utbetalingsdato.toLocalDateTime() }
                ?.firstOrNull { it >= fourMonthsAgo }

        if (utbetaltSiste120Dager != null) {
            log.info("Bruker kvalifiserer til kort søknad via utbetaling $utbetaltSiste120Dager")
            return true
        }

        val planlagtInnen14Dager =
            hendelser
                ?.asSequence()
                ?.filterIsInstance<JsonUtbetaling>()
                ?.filter { it.status == JsonUtbetaling.Status.PLANLAGT_UTBETALING && it.forfallsdato != null }
                ?.map { it.forfallsdato.toLocalDateTime() }
                ?.filter { it >= LocalDateTime.now(clock) }
                ?.firstOrNull { it < in14Days }

        if (planlagtInnen14Dager != null) {
            log.info("Bruker kvalifiserer til kort søknad via planlagt utbetaling $planlagtInnen14Dager")
            return true
        }
        return false
    }

    private fun String.toLocalDateTime() =
        runCatching {
            ZonedDateTime
                .parse(this, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZoneId.of("Europe/Oslo"))
                .toLocalDateTime()
        }.getOrElse { LocalDate.parse(this).atStartOfDay() }
}
