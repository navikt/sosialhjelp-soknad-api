package no.nav.sosialhjelp.soknad.v2.kontakt

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getTokenOrNull
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentlagerService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class KortSoknadUseCaseHandler(
    private val kortSoknadService: KortSoknadService,
    private val dokumentlagerService: DokumentlagerService,
    private val digisosApiService: DigisosApiService,
    private val metadataService: SoknadMetadataService,
    private val unleash: Unleash,
) {
    fun resolveKortSoknad(
        soknadId: UUID,
        oldAdresser: Adresser,
        oldMottaker: NavEnhet?,
        nyMottaker: NavEnhet?,
    ) {
        // I mock overstyrer man dette med valg på forsiden
        // *******Foreløpig endret for testing********
        if (MiljoUtils.isMockAltProfil()) return

        // Ingen endring i kommunenummer og bruker har tatt stilling til det før, trenger ikke vurdere kort søknad
        if (oldMottaker?.hasMottakerNotChanged(nyMottaker, oldAdresser.adressevalg) == true) return

        // Hvis soknad er standard - og det er gjort et adressevalg, så skal den ikke transformeres til kort
        if (isSoknadTypeStandard(soknadId) && oldAdresser.adressevalg != null) return

        val kommunenummer = nyMottaker?.getMottakerKommunenummerOrNull() ?: return

        val qualifiesForKort =
            when {
                isKortSoknadNotEnabled(kommunenummer) -> false
                else -> getTokenOrNull()?.let { isQualifiedFromFiks(it, kommunenummer) }
            }

        when (qualifiesForKort) {
            true -> kortSoknadService.isTransitioningToKort(soknadId)
            false -> kortSoknadService.isTransitioningToStandard(soknadId)
            null -> {
                logger.warn("Token er null, kan ikke sjekke FIKS om bruker har rett på kort søknad")
                false
            }
        }.also { hasTransitioned ->
            if (hasTransitioned) dokumentlagerService.deleteAllDokumenterForSoknad(soknadId)
        }
    }

    private fun isSoknadTypeStandard(soknadId: UUID) =
        metadataService.getSoknadType(soknadId) == SoknadType.STANDARD

    private fun NavEnhet.hasMottakerNotChanged(
        other: NavEnhet?,
        adresseValg: AdresseValg?,
    ): Boolean {
        return kommunenummer == other?.kommunenummer && adresseValg != null
    }

    private fun NavEnhet.getMottakerKommunenummerOrNull(): String? {
        kommunenummer?.let { return it }

        logger.warn("Kommunenummer er null, kan ikke sjekke om bruker har rett på kort søknad")
        return null
    }

    fun isEnabled(kommunenummer: String?): Boolean {
        val context = kommunenummer?.let { UnleashContext.builder().addProperty("kommunenummer", it).build() } ?: UnleashContext.builder().build()
        return unleash.isEnabled("sosialhjelp.soknad.kort_soknad", context, false)
    }

    // semantisk convenience
    private fun isKortSoknadNotEnabled(kommunenummer: String?) = !isEnabled(kommunenummer)

    fun isQualifiedFromFiks(
        token: String,
        kommunenummer: String,
    ): Boolean {
        runCatching {
            return digisosApiService.getSoknaderForUser(token)
                // Viktig med asSequence() her, sånn at den avbryter henting av innsynsfil tidlig hvis den finner et treff i any()
                .asSequence()
                .filter { it.kommunenummer == kommunenummer }
                .sortedByDescending { it.sistEndret }
                .mapNotNull { soknad ->
                    soknad.digisosSoker?.metadata?.let {
                        digisosApiService.getInnsynsfilForSoknad(soknad.fiksDigisosId, it, token)
                    }
                }
                .any { innsynsfil -> innsynsfil.hasRecentOrUpcomingUtbetalinger() }
        }
            .onFailure {
                logger.error("Feil ved henting av innsynsfil fra FIKS", it)
            }
        return false
    }

    private fun JsonDigisosSoker.hasRecentOrUpcomingUtbetalinger(): Boolean {
        val fourMonthsAgo = LocalDateTime.now().minusDays(50)
        val in14Days = LocalDateTime.now().plusDays(14)

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
                ?.filter { it >= LocalDateTime.now() }
                ?.firstOrNull { it < in14Days }

        if (planlagtInnen14Dager != null) {
            logger.info("Bruker kvalifiserer til kort søknad via planlagt utbetaling $planlagtInnen14Dager")
            return true
        }
        return false
    }

    companion object {
        private val logger by logger()
    }
}

private fun String.toLocalDateTime() =
    runCatching {
        ZonedDateTime
            .parse(this, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.of("Europe/Oslo"))
            .toLocalDateTime()
    }.getOrElse { LocalDate.parse(this).atStartOfDay() }
