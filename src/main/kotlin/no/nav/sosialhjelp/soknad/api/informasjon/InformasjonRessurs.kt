package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.api.informasjon.dto.Logg
import no.nav.sosialhjelp.soknad.api.informasjon.dto.NyligInnsendteSoknaderResponse
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering.STRENGT_FORTROLIG_UTLAND
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.Locale
import java.util.Properties

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/informasjon")
class InformasjonRessurs(
    private val messageSource: NavMessageSource,
    private val adresseSokService: AdressesokService,
    private val personService: PersonService,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val pabegynteSoknaderService: PabegynteSoknaderService,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InformasjonRessurs::class.java)
        private val klientlogger = LoggerFactory.getLogger("klientlogger")
        private const val FJORTEN_DAGER: Long = 14
        private const val SOKNADSOSIALHJELP = "soknadsosialhjelp"
    }

    @GetMapping("/fornavn")
    fun hentFornavn(): Map<String, String> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val (fornavn1) = personService.hentPerson(fnr) ?: return emptyMap()
        val fornavnMap = mutableMapOf<String, String>()
        fornavnMap["fornavn"] = fornavn1
        return fornavnMap
    }

    @Unprotected
    @GetMapping("/tekster")
    fun hentTekster(
        @RequestParam("type") queryType: String,
        @RequestParam("sprak") querySprak: String?
    ): Properties {
        var type = queryType
        var sprak = querySprak
        if (sprak == null || sprak.trim { it <= ' ' }.isEmpty()) {
            sprak = "nb_NO"
        }
        if (StringUtils.isNotEmpty(type) && SOKNADSOSIALHJELP != type.lowercase(Locale.getDefault())) {
            val prefiksetType = "soknad" + type.lowercase(Locale.getDefault())
            logger.warn("Type $type matcher ikke et bundlename - forsøker med prefiks $prefiksetType")
            if (SOKNADSOSIALHJELP == prefiksetType) {
                type = prefiksetType
            }
        }
        val locale = LocaleUtils.toLocale(sprak)
        return messageSource.getBundleFor(type, locale)
    }

    @GetMapping("/utslagskriterier/sosialhjelp", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUtslagskriterier(): Map<String, Any> {
        val uid = SubjectHandlerUtils.getUserIdFromToken()
        val adressebeskyttelse = personService.hentAdressebeskyttelse(uid)
        val resultat = mutableMapOf<String, Any>()
        var harTilgang = true
        var sperrekode = ""
        if (FORTROLIG == adressebeskyttelse || STRENGT_FORTROLIG == adressebeskyttelse || STRENGT_FORTROLIG_UTLAND == adressebeskyttelse) {
            harTilgang = false
            sperrekode = "bruker"
        }
        resultat["harTilgang"] = harTilgang
        resultat["sperrekode"] = sperrekode
        return resultat
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
            "INFO" -> klientlogger.info(logg.melding())
            "WARN" -> klientlogger.warn(logg.melding())
            "ERROR" -> klientlogger.error(logg.melding())
            else -> klientlogger.debug(logg.melding())
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
        logger.debug("Henter pabegynte soknader for bruker")
        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr)
    }
}
