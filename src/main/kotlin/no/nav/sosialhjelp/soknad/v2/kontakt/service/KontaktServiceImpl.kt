package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import no.nav.sosialhjelp.soknad.v2.navenhet.NavEnhetService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

// TODO KontaktService? (NavEnhet er jo ikke en adresse per se...)
interface AdresseService {
    fun findAdresser(soknadId: UUID): Adresser

    fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ): Adresser

    fun findMottaker(soknadId: UUID): NavEnhet?

    fun getEnrichment(kommunenummer: String): NavEnhetEnrichment
}

interface TelefonService {
    fun findTelefonInfo(soknadId: UUID): Telefonnummer?

    fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer
}

@Service
class KontaktServiceImpl(
    private val kontaktRepository: KontaktRepository,
    private val kortSoknadService: KortSoknadService,
    private val nyNavEnhetService: NavEnhetService,
    private val kommuneInfoService: KommuneInfoService,
    private val kodeverkService: KodeverkService,
) : AdresseService,
    TelefonService {
    private val logger by logger()

    override fun findTelefonInfo(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.telefonnummer

    override fun updateTelefonnummer(
        soknadId: UUID,
        telefonnummerBruker: String?,
    ): Telefonnummer =
        findOrCreate(soknadId)
            .run { copy(telefonnummer = telefonnummer.copy(fraBruker = telefonnummerBruker)) }
            .let { kontaktRepository.save(it) }
            .telefonnummer

    override fun findAdresser(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)?.adresser
            ?: throw IkkeFunnetException("Fant ikke adresser for soknad")

    @Transactional
    override fun updateBrukerAdresse(
        soknadId: UUID,
        adresseValg: AdresseValg,
        brukerAdresse: Adresse?,
    ): Adresser {
        logger.info(
            "Oppdaterer adresse for $soknadId. " +
                "Adressevalg: $adresseValg, " +
                "Adresse: ${brukerAdresse?.let { "Fylt ut av bruker" }}",
        )

        val oldAdresse = findOrCreate(soknadId)
        val adresse =
            when (adresseValg) {
                AdresseValg.FOLKEREGISTRERT -> oldAdresse.adresser.folkeregistrert
                AdresseValg.MIDLERTIDIG -> oldAdresse.adresser.midlertidig
                AdresseValg.SOKNAD -> brukerAdresse
            }
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val mottaker = adresse?.let { nyNavEnhetService.getNavEnhet(eier, it, adresseValg) }
        return oldAdresse
            .run { copy(adresser = adresser.copy(adressevalg = adresseValg, fraBruker = brukerAdresse), mottaker = mottaker ?: this.mottaker) }
            .let { kontaktRepository.save(it) }
            // Oppdater kort søknad
            .also { adresse ->
                if (!MiljoUtils.isMockAltProfil()) {
                    // Ingen endring i kommunenummer og bruker har tatt stilling til det før, trenger ikke vurdere kort søknad
                    if (oldAdresse.mottaker?.kommunenummer == adresse.mottaker?.kommunenummer && oldAdresse.adresser.adressevalg != null) {
                        return@also
                    }
                    val token = SubjectHandlerUtils.getTokenOrNull()
                    if (token == null) {
                        logger.warn("NyModell: Token er null, kan ikke sjekke om bruker har rett på kort søknad")
                        return@also
                    }
                    val kommunenummer = adresse.mottaker?.kommunenummer
                    if (kommunenummer == null) {
                        logger.warn("NyModell: Kommunenummer er null, kan ikke sjekke om bruker har rett på kort søknad")
                        return@also
                    }

                    val qualifiesForKortSoknad = kortSoknadService.isEnabled(kommunenummer) && kortSoknadService.isQualified(token, kommunenummer)

                    if (qualifiesForKortSoknad) {
                        kortSoknadService.transitionToKort(soknadId)
                    } else {
                        kortSoknadService.transitionToStandard(soknadId)
                    }
                }
            }.adresser
    }

    override fun findMottaker(soknadId: UUID): NavEnhet? {
        val mottaker = kontaktRepository.findByIdOrNull(soknadId)?.mottaker
        if (mottaker == null) {
            logger.warn("NyModell: Ingen nav-enhet funnet for søknad $soknadId")
            return null
        }
        return mottaker
    }

    override fun getEnrichment(kommunenummer: String): NavEnhetEnrichment {
        val isDigisosKommune = kanMottaSoknader(kommunenummer)
        val kommunenavn = kodeverkService.getKommunenavn(kommunenummer)
        return NavEnhetEnrichment(kommunenavn, isDigisosKommune)
    }

    private fun kanMottaSoknader(kommunenummer: String): Boolean {
        val isNyDigisosApiKommuneMedMottakAktivert = kommuneInfoService.kanMottaSoknader(kommunenummer)
        return isNyDigisosApiKommuneMedMottakAktivert
    }

    private fun findOrCreate(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)
            ?: kontaktRepository.save(Kontakt(soknadId))
}

data class NavEnhetEnrichment(
    val kommunenavn: String?,
    val isDigisosKommune: Boolean,
)
