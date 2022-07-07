package no.nav.sosialhjelp.soknad.api.informasjon

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.api.informasjon.dto.KommuneInfoFrontend
import no.nav.sosialhjelp.soknad.api.informasjon.dto.KommunestatusFrontend
import no.nav.sosialhjelp.soknad.api.informasjon.dto.KontaktPersonerFrontend
import no.nav.sosialhjelp.soknad.api.informasjon.dto.Logg
import no.nav.sosialhjelp.soknad.api.informasjon.dto.NyligInnsendteSoknaderResponse
import no.nav.sosialhjelp.soknad.api.informasjon.dto.PabegyntSoknad
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.common.Constants
import no.nav.sosialhjelp.soknad.common.mapper.KommuneTilNavEnhetMapper.digisoskommuner
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
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
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.Collections
import java.util.Locale
import java.util.Properties
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/informasjon")
@Produces(MediaType.APPLICATION_JSON)
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

    @GET
    @Path("/fornavn")
    open fun hentFornavn(): Map<String?, String?>? {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        val (fornavn1) = personService.hentPerson(fnr) ?: return HashMap()
        val fornavnMap: MutableMap<String?, String?> = HashMap()
        fornavnMap["fornavn"] = fornavn1
        return fornavnMap
    }

    @Unprotected
    @GET
    @Path("/tekster")
    open fun hentTekster(@QueryParam("type") queryType: String, @QueryParam("sprak") querySprak: String?): Properties? {
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

    @GET
    @Path("/utslagskriterier/sosialhjelp")
    open fun getUtslagskriterier(): Map<String, Any>? {
        val uid = SubjectHandlerUtils.getUserIdFromToken()
        val adressebeskyttelse = personService.hentAdressebeskyttelse(uid)
        val resultat: MutableMap<String, Any> = java.util.HashMap()
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

    @GET
    @Path("/adressesok")
    open fun adresseSok(@QueryParam("sokestreng") sokestreng: String?): List<AdresseForslag?>? {
        return adresseSokService.sokEtterAdresser(sokestreng)
    }

    @POST
    @Path("/actions/logg")
    open fun loggFraKlient(logg: Logg) {
        when (logg.level) {
            "INFO" -> klientlogger.info(logg.melding())
            "WARN" -> klientlogger.warn(logg.melding())
            "ERROR" -> klientlogger.error(logg.melding())
            else -> klientlogger.debug(logg.melding())
        }
    }

    @GET
    @Path("/kommunelogg")
    open fun triggeKommunelogg(@QueryParam("kommunenummer") kommunenummer: String): String? {
        logger.info("Kommuneinfo trigget for $kommunenummer: ${kommuneInfoService.kommuneInfo(kommunenummer)}")
        return "$kommunenummer er logget. Sjekk kibana"
    }

    @Unprotected
    @GET
    @Path("/kommuneinfo")
    open fun hentKommuneinfo(): Map<String, KommuneInfoFrontend> {
        if (nedetidService.isInnenforNedetid) {
            return emptyMap()
        }
        val manueltPakobledeKommuner = mapManueltPakobledeKommuner(digisoskommuner)
        val digisosKommuner = mapDigisosKommuner(kommuneInfoService.hentAlleKommuneInfo())
        val kunManueltPakobledeKommuner = manueltPakobledeKommuner.keys.filter { !digisosKommuner.containsKey(it) }
        logger.info("/kommuneinfo - Kommuner som kun er manuelt påkoblet via PROD_DIGISOS_KOMMUNER: $kunManueltPakobledeKommuner")
        return mergeManuelleKommunerMedDigisosKommuner(manueltPakobledeKommuner, digisosKommuner)
    }

    @Unprotected
    @GET
    @Path("/kommunestatus")
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

    @GET
    @Path("/harNyligInnsendteSoknader")
    open fun harNyligInnsendteSoknader(): NyligInnsendteSoknaderResponse {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val grense = LocalDateTime.now().minusDays(FJORTEN_DAGER.toLong())
        val nyligSendteSoknader = soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(eier, grense)
        return NyligInnsendteSoknaderResponse(nyligSendteSoknader.size)
    }

    @GET
    @Path("/pabegynteSoknader")
    open fun hentPabegynteSoknader(): List<PabegyntSoknad> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter pabegynte soknader for bruker")
        return pabegynteSoknaderService.hentPabegynteSoknaderForBruker(fnr)
    }

    fun mapManueltPakobledeKommuner(manuelleKommuner: List<String>): Map<String, KommuneInfoFrontend> {
        return manuelleKommuner
            .map {
                KommuneInfoFrontend(
                    kommunenummer = it,
                    kanMottaSoknader = true,
                    kanOppdatereStatus = false
                )
            }
            .associateBy { it.kommunenummer }
    }

    private fun mapManueltPakobledeKommunerTilKommunestatusFrontend(manuelleKommuner: List<String>): Map<String, KommunestatusFrontend> {
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

    fun mapDigisosKommuner(digisosKommuner: Map<String, KommuneInfo>?): MutableMap<String, KommuneInfoFrontend> {
        return digisosKommuner?.values
            ?.filter { it.kanMottaSoknader }
            ?.map {
                KommuneInfoFrontend(
                    it.kommunenummer,
                    it.kanMottaSoknader && !it.harMidlertidigDeaktivertMottak,
                    it.kanOppdatereStatus
                )
            }
            ?.associateBy { it.kommunenummer }
            ?.toMutableMap() ?: mutableMapOf()
    }

    private fun mapDigisosKommunerTilKommunestatus(digisosKommuner: Map<String, KommuneInfo>?): MutableMap<String, KommunestatusFrontend> {
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

    fun mergeManuelleKommunerMedDigisosKommuner(
        manuelleKommuner: Map<String, KommuneInfoFrontend>,
        digisosKommuner: MutableMap<String, KommuneInfoFrontend>
    ): Map<String, KommuneInfoFrontend> {
        manuelleKommuner.forEach { (key: String, value: KommuneInfoFrontend?) ->
            digisosKommuner.putIfAbsent(key, value)
        }
        return digisosKommuner
    }

    private fun mergeManuelleKommunerMedDigisosKommunerKommunestatus(
        manuelleKommuner: Map<String, KommunestatusFrontend>,
        digisosKommuner: MutableMap<String, KommunestatusFrontend>
    ): Map<String, KommunestatusFrontend> {
        manuelleKommuner.forEach { (key: String, value: KommunestatusFrontend?) ->
            digisosKommuner.putIfAbsent(key, value)
        }
        return digisosKommuner
    }
}
