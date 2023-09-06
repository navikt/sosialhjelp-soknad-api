package no.nav.sosialhjelp.soknad.api.informasjon

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.api.informasjon.dto.Logg
import no.nav.sosialhjelp.soknad.api.informasjon.dto.LoggLevel
import no.nav.sosialhjelp.soknad.api.informasjon.dto.NyligInnsendteSoknaderResponse
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG_UTLAND
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Håndterer informasjon om bruker og nylig innsendte/ufullstendige søknader,
 * samt adressesøk.
 */
@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/informasjon")
class InformasjonRessurs(
    private val adresseSokService: AdressesokService,
    private val personService: PersonService,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val pabegynteSoknaderService: PabegynteSoknaderService,
) {

    companion object {
        private val log by logger()
        private val klientlogger = LoggerFactory.getLogger("klientlogger")
        private const val FJORTEN_DAGER: Long = 14
    }

    @GetMapping("/fornavn")
    @Deprecated("Bruk getSessionInfo")
    fun hentFornavn(): Map<String, String> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val (fornavn1) = personService.hentPerson(fnr) ?: return emptyMap()
        val fornavnMap = mutableMapOf<String, String>()
        fornavnMap["fornavn"] = fornavn1
        return fornavnMap
    }

    @GetMapping("/utslagskriterier/sosialhjelp", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Deprecated("Bruk getSessionInfo")
    fun getUtslagskriterier(): Utslagskriterier {
        val uid = SubjectHandlerUtils.getUserIdFromToken()
        val adressebeskyttelse = personService.hentAdressebeskyttelse(uid)

        val (harTilgang, sperrekode) =
            if (FORTROLIG == adressebeskyttelse || STRENGT_FORTROLIG == adressebeskyttelse || STRENGT_FORTROLIG_UTLAND == adressebeskyttelse) {
                Pair(false, Sperrekode.bruker)
            } else {
                Pair(true, null)
            }

        return Utslagskriterier(
            harTilgang,
            sperrekode
        )
    }

    @GetMapping("/adressesok")
    fun adresseSok(
        @RequestParam("sokestreng") sokestreng: String?
    ): List<AdresseForslag> {
        return adresseSokService.sokEtterAdresser(sokestreng)
    }

    @PostMapping("/actions/logg")
    fun loggFraKlient(
        @RequestBody logg: Logg
    ) {
        when (logg.level) {
            LoggLevel.INFO -> klientlogger.info(logg.melding())
            LoggLevel.WARN -> klientlogger.warn(logg.melding())
            LoggLevel.ERROR -> klientlogger.error(logg.melding())
        }
    }

    @GetMapping("/harNyligInnsendteSoknader")
    @Deprecated("Bruk getSessionInfo")
    fun harNyligInnsendteSoknader(): NyligInnsendteSoknaderResponse {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val grense = LocalDateTime.now().minusDays(FJORTEN_DAGER)
        val nyligSendteSoknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(eier, grense)
        return NyligInnsendteSoknaderResponse(nyligSendteSoknader.size)
    }

    @GetMapping("/pabegynteSoknader")
    @Deprecated("Bruk getSessionInfo")
    fun hentPabegynteSoknader(): List<PabegyntSoknad> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        log.debug("Henter pabegynte soknader for bruker")
        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr)
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionResponse {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()

        log.debug("Henter søknadsinfo for bruker")

        val person = personService.hentPerson(fnr)

        // Bør dette ikke egentlig lede til en 500?
        if (person === null) log.error("Fant ikke person for bruker")

        val beskyttelse = personService.hentAdressebeskyttelse(fnr)

        val userBlocked = beskyttelse in listOf(FORTROLIG, STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND)

        val pabegynte = pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr)

        val antallNyligInnsendte =
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(
                fnr,
                LocalDateTime.now().minusDays(FJORTEN_DAGER)
            ).size

        return SessionResponse(
            userBlocked = userBlocked,
            fornavn = person?.fornavn,
            daysBeforeDeletion = FJORTEN_DAGER,
            pabegynte = pabegynte,
            antallNyligInnsendte = antallNyligInnsendte
        )
    }

    enum class Sperrekode {
        bruker
    }

    data class Utslagskriterier(
        val harTilgang: Boolean,
        val sperrekode: Sperrekode?,
    )

    @Schema(description = "Informasjon om en brukers økt")
    data class SessionResponse(
        @Schema(description = "Brukeren har gradert adresse (ihht kap. 6/7)")
        val userBlocked: Boolean,
        @Schema(description = "Brukerens fornavn")
        val fornavn: String?,
        @Schema(description = "Antall dager etter siste endring før søknader slettes")
        val daysBeforeDeletion: Long,
        @Schema(description = "Påbegynte men ikke innleverte søknader")
        val pabegynte: List<PabegyntSoknad>,
        @Schema(description = "Antall nylig innsendte søknader")
        val antallNyligInnsendte: Int,
    )
}
