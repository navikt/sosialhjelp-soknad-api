package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
class KortSoknadService(
    private val digisosApiService: DigisosApiService,
    private val clock: Clock,
) {
    fun qualifies(
        token: String,
        kommunenummer: String,
    ): Boolean = hasRecentSoknadFromFiks(token, kommunenummer) || hasRecentOrUpcomingUtbetalinger(token, kommunenummer)

    private fun hasRecentSoknadFromFiks(
        token: String,
        kommunenummer: String,
    ): Boolean {
        val innsynsfiler =
            getInnsynsfilerForKommune(token, kommunenummer)
        val mottattTimestamps =
            innsynsfiler.flatMap { innsynsfil ->
                innsynsfil.hendelser
                    ?.filter { it is JsonSoknadsStatus && it.status == JsonSoknadsStatus.Status.MOTTATT }
                    ?.mapNotNull { it.hendelsestidspunkt } ?: emptyList()
            }
        return mottattTimestamps.any { it.toLocalDateTime() >= LocalDateTime.now(clock).minusDays(120) }
    }

    private fun hasRecentOrUpcomingUtbetalinger(
        token: String,
        kommunenummer: String,
    ): Boolean {
        val fourMonthsAgo = LocalDateTime.now(clock).minusDays(120)
        val in14Days = LocalDateTime.now(clock).plusDays(14)

        val innsynsfiler =
            getInnsynsfilerForKommune(token, kommunenummer)

        val utbetalte =
            innsynsfiler.flatMap { innsynsfil ->
                innsynsfil
                    .hendelser
                    ?.filterIsInstance<JsonUtbetaling>()
                    ?.filter { it.status == JsonUtbetaling.Status.UTBETALT && it.utbetalingsdato != null }
                    ?.map { it.utbetalingsdato.toLocalDateTime() } ?: emptyList()
            }

        if (utbetalte.any { it >= fourMonthsAgo }) {
            return true
        }

        val planlagte =
            innsynsfiler.flatMap { innsynsfil ->
                innsynsfil
                    .hendelser
                    ?.filterIsInstance<JsonUtbetaling>()
                    ?.filter { it.status == JsonUtbetaling.Status.PLANLAGT_UTBETALING && it.forfallsdato != null }
                    ?.map { it.forfallsdato.toLocalDateTime() } ?: emptyList()
            }

        return planlagte.any { it < in14Days }
    }

    private fun getInnsynsfilerForKommune(
        token: String,
        kommunenummer: String,
    ): List<JsonDigisosSoker> =
        digisosApiService.getSoknaderForUser(token).filter { it.kommunenummer == kommunenummer }.mapNotNull { soknad ->
            soknad.digisosSoker?.metadata?.let {
                digisosApiService.getInnsynsfilForSoknad(soknad.fiksDigisosId, it, token)
            }
        }

    private fun String.toLocalDateTime() =
        runCatching {
            ZonedDateTime
                .parse(this, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZoneId.of("Europe/Oslo"))
                .toLocalDateTime()
        }.getOrElse { LocalDate.parse(this).atStartOfDay() }
}
