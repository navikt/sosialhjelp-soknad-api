package no.nav.sosialhjelp.soknad.api.informasjon

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
 * Klassen håndterer rest kall for å hente informasjon
 */
@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
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
    fun hentFornavn(): Map<String, String> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val (fornavn1) = personService.hentPerson(fnr) ?: return emptyMap()
        val fornavnMap = mutableMapOf<String, String>()
        fornavnMap["fornavn"] = fornavn1
        return fornavnMap
    }

    @GetMapping("/utslagskriterier/sosialhjelp", produces = [MediaType.APPLICATION_JSON_VALUE])
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
    fun harNyligInnsendteSoknader(): NyligInnsendteSoknaderResponse {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val grense = LocalDateTime.now().minusDays(FJORTEN_DAGER)
        val nyligSendteSoknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(eier, grense)
        return NyligInnsendteSoknaderResponse(nyligSendteSoknader.size)
    }

    @GetMapping("/pabegynteSoknader")
    fun hentPabegynteSoknader(): List<PabegyntSoknad> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        log.debug("Henter pabegynte soknader for bruker")
        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr)
    }

    enum class Sperrekode {
        bruker
    }

    data class Utslagskriterier(
        val harTilgang: Boolean,
        val sperrekode: Sperrekode?,
    )
}
