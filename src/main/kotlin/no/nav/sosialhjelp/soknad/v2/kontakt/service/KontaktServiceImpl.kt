package no.nav.sosialhjelp.soknad.v2.kontakt.service

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.KortSoknadService
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresse
import no.nav.sosialhjelp.soknad.v2.kontakt.AdresseValg
import no.nav.sosialhjelp.soknad.v2.kontakt.Adresser
import no.nav.sosialhjelp.soknad.v2.kontakt.Kontakt
import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.NavEnhet
import no.nav.sosialhjelp.soknad.v2.kontakt.Telefonnummer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
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
        return oldAdresse
            .run { copy(adresser = adresser.copy(adressevalg = adresseValg, fraBruker = brukerAdresse)) }
            .let { kontaktRepository.save(it) }
            .also { adresse ->
                // Ingen endring i kommunenummer, trenger ikke vurdere kort søknad
                if (oldAdresse.mottaker.kommunenummer == adresse.mottaker.kommunenummer) {
                    return@also
                }
                val token = SubjectHandlerUtils.getTokenOrNull()
                if (token == null) {
                    logger.warn("NyModell: Token er null, kan ikke sjekke om bruker har rett på kort søknad")
                    return@also
                }
                val kommunenummer = adresse.mottaker.kommunenummer
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
            }.adresser
    }

    override fun findMottaker(soknadId: UUID) = kontaktRepository.findByIdOrNull(soknadId)?.mottaker

    private fun findOrCreate(soknadId: UUID) =
        kontaktRepository.findByIdOrNull(soknadId)
            ?: kontaktRepository.save(Kontakt(soknadId))
}
