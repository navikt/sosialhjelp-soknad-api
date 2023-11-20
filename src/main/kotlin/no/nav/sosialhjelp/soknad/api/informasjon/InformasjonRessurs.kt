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
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG_UTLAND
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.UGRADERT
import no.nav.sosialhjelp.soknad.repository.soknadmetadata.SoknadMetadataRepository
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as getUser

/**
 * Håndterer informasjon om bruker og nylig innsendte/ufullstendige søknader,
 * samt adressesøk.
 */
@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true
)
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
        val (fornavn1) = personService.hentPerson(getUser()) ?: return emptyMap()
        val fornavnMap = mutableMapOf<String, String>()
        fornavnMap["fornavn"] = fornavn1
        return fornavnMap
    }

    @GetMapping("/utslagskriterier/sosialhjelp", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Deprecated("Bruk getSessionInfo")
    fun getUtslagskriterier(): Utslagskriterier {
        val adressebeskyttelse = personService.hentAdressebeskyttelse(getUser())

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
        val grense = LocalDateTime.now().minusDays(FJORTEN_DAGER)
        val nylige =
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(getUser(), grense)
        return NyligInnsendteSoknaderResponse(nylige.size)
    }

    @GetMapping("/pabegynteSoknader")
    @Deprecated("Bruk getSessionInfo")
    fun hentPabegynteSoknader(): List<PabegyntSoknad> {
        log.debug("Henter pabegynte soknader for bruker")
        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(getUser())
    }

    @GetMapping("/session")
    fun getSessionInfo(): SessionResponse {
        val eier = getUser()

        log.debug("Henter søknadsinfo for bruker")

        val person = personService.hentPerson(eier)

        // Egentlig bør vel hentPerson kaste en exception dersom en bruker ikke finnes
        // for en gitt ID. I første omgang nøyer vi oss med å logge en feilmelding
        // men hentPerson bør nok bli noe mer deterministisk.
        if (person === null) log.error("Fant ikke person for bruker")

        val beskyttelsesgrad = personService.hentAdressebeskyttelse(eier) ?: UGRADERT

        val open = pabegynteSoknaderService.hentPabegynteSoknaderForBruker(eier)

        val numRecentlySent =
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(
                eier,
                LocalDateTime.now().minusDays(FJORTEN_DAGER)
            ).size

        return SessionResponse(
            userBlocked = beskyttelsesgrad != UGRADERT,
            fornavn = person?.fornavn,
            daysBeforeDeletion = FJORTEN_DAGER,
            open = open,
            numRecentlySent = numRecentlySent
        )
    }

    enum class Sperrekode {
        bruker
    }

    data class Utslagskriterier(
        val harTilgang: Boolean,
        val sperrekode: Sperrekode?,
    )

    @Schema(description = "Informasjon om brukerøkt")
    data class SessionResponse(
        @Schema(description = "Bruker har adressebeskyttelse og kan ikke bruke digital søknad")
        val userBlocked: Boolean,
        @Schema(description = "Brukerens fornavn")
        val fornavn: String?,
        @Schema(description = "Antall dager etter siste endring før søknader slettes")
        val daysBeforeDeletion: Long,
        @Schema(description = "Påbegynte men ikke innleverte søknader")
        val open: List<PabegyntSoknad>,
        @Schema(description = "Antall nylig innsendte søknader")
        val numRecentlySent: Int,
    )
}
