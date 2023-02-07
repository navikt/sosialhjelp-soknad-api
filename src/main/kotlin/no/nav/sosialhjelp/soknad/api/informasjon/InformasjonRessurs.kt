package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.api.informasjon.dto.KommunestatusFrontend
import no.nav.sosialhjelp.soknad.api.informasjon.dto.KontaktPersonerFrontend
import no.nav.sosialhjelp.soknad.api.informasjon.dto.Logg
import no.nav.sosialhjelp.soknad.api.informasjon.dto.NyligInnsendteSoknaderResponse
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper.digisoskommuner
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
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
import java.util.Collections
import java.util.Locale
import java.util.Properties

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/informasjon")
open class InformasjonRessurs(
    private val messageSource: NavMessageSource,
    private val adresseSokService: AdressesokService,
    private val kommuneInfoService: KommuneInfoService,
    private val personService: PersonService,
    private val soknadMetadataRepository: SoknadMetadataRepository,
    private val pabegynteSoknaderService: PabegynteSoknaderService,
    private val nedetidService: NedetidService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InformasjonRessurs::class.java)
        private val klientlogger = LoggerFactory.getLogger("klientlogger")
        private const val FJORTEN_DAGER = 14
        private const val SOKNADSOSIALHJELP = "soknadsosialhjelp"
    }

    @GetMapping("/fornavn")
    open fun hentFornavn(): Map<String, String> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val (fornavn1) = personService.hentPerson(fnr) ?: return emptyMap()
        val fornavnMap = mutableMapOf<String, String>()
        fornavnMap["fornavn"] = fornavn1
        return fornavnMap
    }

    @Unprotected
    @GetMapping("/tekster")
    open fun hentTekster(
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
    open fun getUtslagskriterier(): Utslagskriterier {
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
    open fun adresseSok(
        @RequestParam("sokestreng") sokestreng: String?
    ): List<AdresseForslag> {
        return adresseSokService.sokEtterAdresser(sokestreng)
    }

    @PostMapping("/actions/logg")
    open fun loggFraKlient(
        @RequestBody logg: Logg
    ) {
        when (logg.level) {
            "INFO" -> klientlogger.info(logg.melding())
            "WARN" -> klientlogger.warn(logg.melding())
            "ERROR" -> klientlogger.error(logg.melding())
            else -> klientlogger.debug(logg.melding())
        }
    }

    @GetMapping("/kommunelogg")
    open fun triggeKommunelogg(
        @RequestParam("kommunenummer") kommunenummer: String
    ): String? {
        logger.info("Kommuneinfo trigget for $kommunenummer: ${kommuneInfoService.kommuneInfo(kommunenummer)}")
        return "$kommunenummer er logget. Sjekk kibana"
    }

    @GetMapping("/kommunestatus")
    @ProtectedWithClaims(issuer = Constants.AZUREAD)
    open fun hentKommunestatus(): Map<String, KommunestatusFrontend> {
        if (nedetidService.isInnenforNedetid) {
            return emptyMap()
        }
        val manueltPakobledeKommuner = mapManueltPakobledeKommunerTilKommunestatusFrontend(digisoskommuner)
        val digisosKommuner = mapDigisosKommunerTilKommunestatus(kommuneInfoService.hentAlleKommuneInfo())
        val kunManueltPakobledeKommuner = manueltPakobledeKommuner.keys.filter { !digisosKommuner.containsKey(it) }
        logger.info("/kommunestatus - Kommuner som kun er manuelt påkoblet via PROD_DIGISOS_KOMMUNER: $kunManueltPakobledeKommuner")
        return mergeManuelleKommunerMedDigisosKommunerKommunestatus(manueltPakobledeKommuner, digisosKommuner)
    }

    @GetMapping("/harNyligInnsendteSoknader")
    open fun harNyligInnsendteSoknader(): NyligInnsendteSoknaderResponse {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val grense = LocalDateTime.now().minusDays(FJORTEN_DAGER.toLong())
        val nyligSendteSoknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(eier, grense)
        return NyligInnsendteSoknaderResponse(nyligSendteSoknader.size)
    }

    @GetMapping("/pabegynteSoknader")
    open fun hentPabegynteSoknader(): List<PabegyntSoknad> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter pabegynte soknader for bruker")
        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr)
    }

    fun mapManueltPakobledeKommunerTilKommunestatusFrontend(manuelleKommuner: List<String>): Map<String, KommunestatusFrontend> {
        return manuelleKommuner
            .map {
                KommunestatusFrontend(
                    kommunenummer = it,
                    kanMottaSoknader = true,
                    kanOppdatereStatus = false
                )
            }
            .associateBy { it.kommunenummer }
    }

    fun mapDigisosKommunerTilKommunestatus(digisosKommuner: Map<String, KommuneInfo>?): MutableMap<String, KommunestatusFrontend> {
        return digisosKommuner?.values
            ?.map {
                KommunestatusFrontend(
                    it.kommunenummer,
                    it.kanMottaSoknader,
                    it.kanOppdatereStatus,
                    it.harMidlertidigDeaktivertMottak,
                    it.harMidlertidigDeaktivertOppdateringer,
                    it.harNksTilgang,
                    it.behandlingsansvarlig,
                    KontaktPersonerFrontend(
                        it.kontaktpersoner?.fagansvarligEpost ?: Collections.emptyList(),
                        it.kontaktpersoner?.tekniskAnsvarligEpost ?: Collections.emptyList()
                    )
                )
            }
            ?.associateBy { it.kommunenummer }
            ?.toMutableMap() ?: mutableMapOf()
    }

    fun mergeManuelleKommunerMedDigisosKommunerKommunestatus(
        manuelleKommuner: Map<String, KommunestatusFrontend>,
        digisosKommuner: MutableMap<String, KommunestatusFrontend>
    ): Map<String, KommunestatusFrontend> {
        manuelleKommuner.forEach { (key: String, value: KommunestatusFrontend?) ->
            digisosKommuner.putIfAbsent(key, value)
        }
        return digisosKommuner
    }

    enum class Sperrekode {
        bruker
    }

    data class Utslagskriterier(
        var harTilgang: Boolean,
        var sperrekode: Sperrekode?,
    )
}
