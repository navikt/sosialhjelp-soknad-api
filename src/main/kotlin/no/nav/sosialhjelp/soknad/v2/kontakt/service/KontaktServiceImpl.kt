package no.nav.sosialhjelp.soknad.v2.kontakt.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
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
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as personId

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
) : AdresseService, TelefonService {
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

        val mottaker =
            when (adresseValg) {
                AdresseValg.FOLKEREGISTRERT -> oldAdresse.adresser.folkeregistrert
                AdresseValg.MIDLERTIDIG -> oldAdresse.adresser.midlertidig
                AdresseValg.SOKNAD -> brukerAdresse
            }
                ?.let { nyNavEnhetService.getNavEnhet(personId(), it, adresseValg) }

        if (mottaker == null) {
            logger.warn("NyModell: Fant ikke mottaker ved oppdatering av søknad $soknadId")
        }

        val adresse =
            oldAdresse
                .run {
                    copy(
                        adresser = adresser.copy(adressevalg = adresseValg, fraBruker = brukerAdresse),
                        mottaker = mottaker ?: this.mottaker,
                    )
                }
                .let { kontaktRepository.save(it) }

        // TODO Fjern
        logger.info("Adresse før oppdatering: ${jacksonObjectMapper().writeValueAsString(oldAdresse)}")
        logger.info("Adresse etter oppdatering: ${jacksonObjectMapper().writeValueAsString(adresse)}")

        kortSoknadService.resolveKortSoknad(oldAdresse, adresse)

        return adresse.adresser

//        return oldAdresse
//            .run {
//                copy(
//                    adresser = adresser.copy(adressevalg = adresseValg, fraBruker = brukerAdresse),
//                    mottaker = mottaker ?: this.mottaker)
//            }
//            .let { kontaktRepository.save(it) }
//            // Oppdater kort søknad
//            .also { adresse ->
//                if (!MiljoUtils.isMockAltProfil()) {
//                    // Ingen endring i kommunenummer og bruker har tatt stilling til det før, trenger ikke vurdere kort søknad
//                    if (
//                        oldAdresse.mottaker?.kommunenummer == adresse.mottaker?.kommunenummer &&
//                        oldAdresse.adresser.adressevalg != null
//                    ) {
//                        logger.info("oldAdresse.mottaker?.kommunenummer: ${oldAdresse.mottaker?.kommunenummer}, " +
//                                "adresse.mottaker?.kommunenummer: ${adresse.mottaker?.kommunenummer}, " +
//                                "oldAdresse.adresser.adressevalg: ${oldAdresse.adresser.adressevalg}")
//                        return@also
//                    }
//                    val token = getTokenOrNull()
//                    if (token == null) {
//                        logger.warn("NyModell: Token er null, kan ikke sjekke om bruker har rett på kort søknad")
//                        return@also
//                    }
//                    val kommunenummer = adresse.mottaker?.kommunenummer
//                    if (kommunenummer == null) {
//                        logger.warn("NyModell: Kommunenummer er null, kan ikke sjekke om bruker har rett på kort søknad")
//                        return@also
//                    }
//
//                    val qualifiesForKortSoknad =
//                        kortSoknadService.isEnabled(kommunenummer) &&
//                                kortSoknadService.isQualified(token, kommunenummer)
//
//                    // TODO Ekstra logging
//                    logger.info("NyModell: Bruker kvalifiserer til kort søknad: $qualifiesForKortSoknad")
//
//                    if (qualifiesForKortSoknad) kortSoknadService.transitionToKort(soknadId)
//                    else kortSoknadService.transitionToStandard(soknadId)
//                }
//            }.adresser
    }

    override fun findMottaker(soknadId: UUID): NavEnhet? {
        return kontaktRepository.findByIdOrNull(soknadId)
            ?.let {
                // TODO Er vel strengt talt en uopprettelig feil isåfall?!
                if (it.mottaker == null && it.adresser.adressevalg != null) {
                    logger.error("NyModell: Fant ikke mottaker for søknad $soknadId")
                    null
                } else {
                    it.mottaker
                }
            }
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
