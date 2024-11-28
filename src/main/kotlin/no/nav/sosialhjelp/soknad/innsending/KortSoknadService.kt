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
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

@Component
class KortSoknadService(
    private val digisosApiService: DigisosApiService,
    private val clock: Clock,
    private val soknadService: SoknadService,
    private val dokumentasjonService: DokumentasjonService,
    private val soknadMetadataService: SoknadMetadataService,
    private val unleash: Unleash,
) {
    private val logger by logger()

    fun transitionToKort(soknadId: UUID) {
        if (soknadMetadataService.getMetadataForSoknad(soknadId).soknadType == SoknadType.KORT) return

        soknadMetadataService.updateSoknadType(soknadId, SoknadType.KORT)
        logger.info("Transitioning soknad $soknadId to kort")

        // Hvis en søknad er kort -> fjern forventet dokumentasjon og opprett obligatorisk dokumentasjon
        dokumentasjonService.resetForventetDokumentasjon(soknadId)
        dokumentasjonService.opprettObligatoriskDokumentasjon(soknadId, SoknadType.KORT)

        soknadService.updateKortSoknad(soknadId, true)
    }

    fun transitionToStandard(soknadId: UUID) {
        if (soknadMetadataService.getMetadataForSoknad(soknadId).soknadType == SoknadType.STANDARD) return

        soknadMetadataService.updateSoknadType(soknadId, SoknadType.STANDARD)

        // Hvis en soknad skal transformeres til standard (igjen) -> fjern kun BEHOV og lett til SKATTEMELDING
        dokumentasjonService.fjernForventetDokumentasjon(soknadId, AnnenDokumentasjonType.BEHOV)
        dokumentasjonService.opprettDokumentasjon(soknadId, AnnenDokumentasjonType.SKATTEMELDING)

        soknadService.updateKortSoknad(soknadId, false)
    }

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
                .any { innsynsfil ->
                    innsynsfil.hasRecentSoknadFromFiks() || innsynsfil.hasRecentOrUpcomingUtbetalinger()
                }
        }
            .onFailure {
                logger.error("NyModell: Feil ved henting av innsynsfil fra FIKS", it)
            }
        return false
    }

    fun isEnabled(kommunenummer: String?): Boolean {
        val context = kommunenummer?.let { UnleashContext.builder().addProperty("kommunenummer", it).build() } ?: UnleashContext.builder().build()
        return unleash.isEnabled("sosialhjelp.soknad.kort_soknad", context, false)
    }

    // semantisk convenience
    private fun isKortSoknadNotEnabled(kommunenummer: String?) = !isEnabled(kommunenummer)

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
    fun resolveKortSoknad(
        oldKontakt: Kontakt,
        updatedKontakt: Kontakt,
    ) {
        // I mock overstyrer man dette med valg på forsiden
        if (MiljoUtils.isMockAltProfil()) return

        // Ingen endring i kommunenummer og bruker har tatt stilling til det før, trenger ikke vurdere kort søknad
        if (oldKontakt.hasMottakerNotChanged(updatedKontakt.mottaker)) return

        // Hvis soknad er standard - og det er gjort et adressevalg, så skal den ikke transformeres til kort
        if (isSoknadTypeStandard(oldKontakt.soknadId) && oldKontakt.adresser.adressevalg != null) return

        val kommunenummer = updatedKontakt.getMottakerKommunenummerOrNull() ?: return

        val qualifiesForKort =
            when {
                isKortSoknadNotEnabled(kommunenummer) -> false
                isQualifiedFromMetadata(oldKontakt.soknadId, kommunenummer) -> true
                else -> getTokenOrNull()?.let { isQualifiedFromFiks(it, kommunenummer) }
            }

        when (qualifiesForKort) {
            true -> transitionToKort(updatedKontakt.soknadId)
            false -> transitionToStandard(updatedKontakt.soknadId)
            null -> logger.warn("NyModell: Token er null, kan ikke sjekke FIKS om bruker har rett på kort søknad")
        }
    }

    private fun isSoknadTypeStandard(soknadId: UUID) =
        soknadMetadataService.getSoknadType(soknadId) == SoknadType.STANDARD

    private fun Kontakt.hasMottakerNotChanged(other: NavEnhet?): Boolean {
        return mottaker?.kommunenummer == other?.kommunenummer && adresser.adressevalg != null
    }

    private fun Kontakt.getMottakerKommunenummerOrNull(): String? {
        mottaker?.kommunenummer?.let { return it }

        logger.warn("NyModell: Kommunenummer er null, kan ikke sjekke om bruker har rett på kort søknad")
        return null
    }

    // sjekker om bruker har rett på kort søknad basert på metadata
    private fun isQualifiedFromMetadata(
        soknadId: UUID,
        kommunenummer: String,
    ): Boolean {
        soknadMetadataService.getAllMetadataForPerson(personId())
            .asSequence()
            .filter { metadata -> metadata.status == SoknadStatus.SENDT || metadata.status == SoknadStatus.MOTTATT_FSL }
            .filter { metadata -> metadata.mottakerKommunenummer == kommunenummer }
            .filter { metadata -> metadata.soknadId != soknadId }
            .sortedByDescending { metadata -> metadata.tidspunkt.sendtInn }
            .mapNotNull { metadata -> metadata.tidspunkt.sendtInn }
            .firstOrNull { sendtInn -> sendtInn >= LocalDateTime.now(clock).minusDays(120) }
            ?.let { sendtInn ->
                logger.info("Kvalifiserer til kort søknad via søknad mottatt: $sendtInn")
                return true
            }

        return false
    }
}
