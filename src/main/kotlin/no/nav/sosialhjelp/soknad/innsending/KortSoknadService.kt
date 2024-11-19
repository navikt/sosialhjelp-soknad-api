package no.nav.sosialhjelp.soknad.innsending

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getTokenOrNull
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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
    private val dokumentasjonService: DokumentasjonService,
    private val dokumentService: DokumentService,
    private val unleash: Unleash,
) {
    private val logger by logger()

    @Transactional
    fun transitionToKort(soknadId: UUID) {
        logger.info("Transitioning soknad $soknadId to kort")
        dokumentasjonService.resetForventetDokumentasjon(soknadId)

        dokumentasjonService.opprettObligatoriskDokumentasjon(soknadId, SoknadType.KORT)
        soknadService.updateKortSoknad(soknadId, true)
    }

    @Transactional
    fun transitionToStandard(soknadId: UUID) {
        dokumentasjonService.resetForventetDokumentasjon(soknadId)

        dokumentasjonService.opprettObligatoriskDokumentasjon(soknadId, SoknadType.STANDARD)
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
            logger.info("Bruker kvaliserer til kort søknad via søknad mottatt $mottattSiste120Dager")
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
            logger.info("Bruker kvalifiserer til kort søknad via utbetaling $utbetaltSiste120Dager")
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
            logger.info("Bruker kvalifiserer til kort søknad via planlagt utbetaling $planlagtInnen14Dager")
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

    // TODO Håndter transaksjonsscope (ps: skjer eksterne kall i denne)
    @Transactional
    fun resolveKortSoknad(
        oldAdresse: Kontakt,
        updatedAdresse: Kontakt,
    ) {
        if (!MiljoUtils.isMockAltProfil()) {
            // Ingen endring i kommunenummer og bruker har tatt stilling til det før, trenger ikke vurdere kort søknad
            if (
                oldAdresse.mottaker?.kommunenummer == updatedAdresse.mottaker?.kommunenummer &&
                oldAdresse.adresser.adressevalg != null
            ) {
                logger.info(
                    "oldAdresse.mottaker?.kommunenummer: ${oldAdresse.mottaker?.kommunenummer}, " +
                        "adresse.mottaker?.kommunenummer: ${updatedAdresse.mottaker?.kommunenummer}, " +
                        "oldAdresse.adresser.adressevalg: ${oldAdresse.adresser.adressevalg}",
                )
                return
            }
            val token = getTokenOrNull()
            if (token == null) {
                logger.warn("NyModell: Token er null, kan ikke sjekke om bruker har rett på kort søknad")
                return
            }
            val kommunenummer = updatedAdresse.mottaker?.kommunenummer
            if (kommunenummer == null) {
                logger.warn("NyModell: Kommunenummer er null, kan ikke sjekke om bruker har rett på kort søknad")
                return
            }

            val qualifiesForKortSoknad = isEnabled(kommunenummer) && isQualified(token, kommunenummer)

            // TODO Ekstra logging
            logger.info("NyModell: Bruker kvalifiserer til kort søknad: $qualifiesForKortSoknad")

            if (qualifiesForKortSoknad) {
                transitionToKort(updatedAdresse.soknadId)
            } else {
                transitionToStandard(updatedAdresse.soknadId)
            }
        }
    }
}
