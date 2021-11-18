package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslag
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseForslagType
import no.nav.sosialhjelp.soknad.business.service.adressesok.AdresseSokService
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.consumer.fiks.KommuneInfoService
import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.bydel.BydelService
import no.nav.sosialhjelp.soknad.domain.model.mock.MockUtils
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import java.util.stream.Collectors
import javax.ws.rs.GET
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/soknader/{behandlingsId}/personalia")
@Timed
@Produces(MediaType.APPLICATION_JSON)
open class NavEnhetRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetService: NavEnhetService,
    private val kommuneInfoService: KommuneInfoService,
    private val bydelService: BydelService,
    private val adresseSokService: AdresseSokService,
    private val geografiskTilknytningService: GeografiskTilknytningService,
    private val kodeverkService: KodeverkService
) {

    @GET
    @Path("/navEnheter")
    open fun hentNavEnheter(@PathParam("behandlingsId") behandlingsId: String): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad.soknad
        val valgtEnhetNr = soknad.mottaker.enhetsnummer
        val oppholdsadresse = soknad.data.personalia.oppholdsadresse
        val adresseValg = utledAdresseValg(oppholdsadresse)
        return findSoknadsmottaker(eier, soknad, adresseValg, valgtEnhetNr)
    }

    private fun utledAdresseValg(oppholdsadresse: JsonAdresse?): String? {
        return if (oppholdsadresse == null) {
            null
        } else if (oppholdsadresse.adresseValg == null) {
            null
        } else {
            oppholdsadresse.adresseValg.toString()
        }
    }

    @GET
    @Path("/navEnhet")
    open fun hentValgtNavEnhet(@PathParam("behandlingsId") behandlingsId: String): NavEnhetFrontend? {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandler.getUserId()
        val soknadsmottaker = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad.soknad.mottaker
        val kommunenummer = soknadsmottaker.kommunenummer
        return if (kommunenummer.isNullOrEmpty() || soknadsmottaker.navEnhetsnavn.isNullOrEmpty()) {
            null
        } else {
            NavEnhetFrontend(
                enhetsnr = soknadsmottaker.enhetsnummer,
                enhetsnavn = getEnhetsnavnFromNavEnhetsnavn(soknadsmottaker.navEnhetsnavn),
                kommunenavn = getKommunenavnFromNavEnhetsnavn(soknadsmottaker.navEnhetsnavn),
                kommuneNr = kommunenummer,
                isMottakDeaktivert = !isDigisosKommune(kommunenummer),
                isMottakMidlertidigDeaktivert = kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer),
                orgnr = KommuneTilNavEnhetMapper.getOrganisasjonsnummer(soknadsmottaker.enhetsnummer), // Brukes ikke etter at kommunene er på Fiks konfigurasjon og burde ikke bli brukt av frontend.
                valgt = true
            )
        }
    }

    @PUT
    @Path("/navEnheter")
    open fun updateNavEnhet(@PathParam("behandlingsId") behandlingsId: String, navEnhetFrontend: NavEnhetFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandler.getUserId()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        soknad.jsonInternalSoknad.mottaker = no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withOrganisasjonsnummer(navEnhetFrontend.orgnr)
        soknad.jsonInternalSoknad.soknad.mottaker = JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withEnhetsnummer(navEnhetFrontend.enhetsnr)
            .withKommunenummer(navEnhetFrontend.kommuneNr)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    private fun createNavEnhetsnavn(enhetsnavn: String, kommunenavn: String): String {
        return enhetsnavn + SPLITTER + kommunenavn
    }

    private fun getEnhetsnavnFromNavEnhetsnavn(navEnhetsnavn: String): String {
        return navEnhetsnavn.split(SPLITTER)[0]
    }

    private fun getKommunenavnFromNavEnhetsnavn(navEnhetsnavn: String): String {
        return navEnhetsnavn.split(SPLITTER)[1]
    }

    open fun findSoknadsmottaker(
        eier: String,
        soknad: JsonSoknad,
        valg: String?,
        valgtEnhetNr: String?
    ): List<NavEnhetFrontend>? {
        val personalia = soknad.data.personalia
        return if ("folkeregistrert" == valg) {
            try {
                finnNavEnhetFraGT(eier, personalia, valgtEnhetNr)
            } catch (e: Exception) {
                log.warn("Noe feilet ved utleding av Nav-kontor ut fra GT hentet fra PDL -> fallback til adressesøk-løsning", e)
                finnNavEnhetFraAdresse(personalia, valg, valgtEnhetNr)
            }
        } else finnNavEnhetFraAdresse(personalia, valg, valgtEnhetNr)
    }

    private fun finnNavEnhetFraGT(
        ident: String,
        personalia: JsonPersonalia,
        valgtEnhetNr: String?
    ): List<NavEnhetFrontend> {
        val kommunenummer = getKommunenummer(personalia.oppholdsadresse) ?: return emptyList()
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        val navEnhet = navEnhetService.getEnhetForGt(geografiskTilknytning)
        val navEnhetFrontend = mapToNavEnhetFrontend(navEnhet, geografiskTilknytning, kommunenummer, valgtEnhetNr)
        return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
    }

    private fun mapToNavEnhetFrontend(
        navEnhet: NavEnhet?,
        geografiskTilknytning: String?,
        kommunenummer: String?,
        valgtEnhetNr: String?
    ): NavEnhetFrontend? {
        var valgtKommunenummer: String? = kommunenummer
        if (navEnhet == null) {
            log.warn("Kunne ikke hente NAV-enhet: $geografiskTilknytning , i kommune: $kommunenummer")
            return null
        }
        if (valgtKommunenummer == null || valgtKommunenummer.length != 4) {
            log.warn("Kommunenummer hadde ikke 4 tegn, var $valgtKommunenummer")
            return null
        }
        if (ServiceUtils.isNonProduction() && MockUtils.isAlltidHentKommuneInfoFraNavTestkommune()) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD")
            valgtKommunenummer = "3002"
        }
        val isDigisosKommune = isDigisosKommune(valgtKommunenummer)
        val sosialOrgnr = if (isDigisosKommune) navEnhet.sosialOrgNr else null
        val enhetNr = if (isDigisosKommune) navEnhet.enhetNr else null
        val valgt = enhetNr != null && enhetNr == valgtEnhetNr
        val kommunenavn = kodeverkService.getKommunenavn(valgtKommunenummer)
        return NavEnhetFrontend(
            enhetsnr = enhetNr,
            enhetsnavn = navEnhet.navn,
            kommunenavn = kommuneInfoService.getBehandlingskommune(valgtKommunenummer, kommunenavn),
            orgnr = sosialOrgnr,
            valgt = valgt,
            kommuneNr = valgtKommunenummer,
            isMottakDeaktivert = !isDigisosKommune,
            isMottakMidlertidigDeaktivert = kommuneInfoService.harMidlertidigDeaktivertMottak(valgtKommunenummer)
        )
    }

    private fun finnNavEnhetFraAdresse(
        personalia: JsonPersonalia,
        valg: String?,
        valgtEnhetNr: String?
    ): List<NavEnhetFrontend>? {
        val adresseForslagene = adresseSokService.finnAdresseFraSoknad(personalia, valg)
        /*
         * Vi fjerner nå duplikate NAV-enheter med forskjellige bydelsnumre gjennom
         * bruk av distinct. Hvis det er viktig med riktig bydelsnummer bør dette kallet
         * fjernes og brukeren må besvare hvilken bydel han/hun oppholder seg i.
         */
        val navEnhetFrontendListe: MutableList<NavEnhetFrontend> = mutableListOf()
        for (adresseForslag in adresseForslagene) {
            if (adresseForslag.type != null && adresseForslag.type == AdresseForslagType.MATRIKKELADRESSE) {
                val navenheter = navEnhetService.getEnheterForKommunenummer(adresseForslag.kommunenummer)
                navenheter!!
                    .forEach {
                        addToNavEnhetFrontendListe(
                            navEnhetFrontendListe,
                            adresseForslag.geografiskTilknytning,
                            adresseForslag,
                            it,
                            valgtEnhetNr
                        )
                    }
                log.info("Matrikkeladresse ble brukt. Returnerer ${navenheter.size} navenheter")
            } else {
                val geografiskTilknytning = getGeografiskTilknytningFromAdresseForslag(adresseForslag)
                val navEnhet = navEnhetService.getEnhetForGt(geografiskTilknytning)
                addToNavEnhetFrontendListe(
                    navEnhetFrontendListe,
                    geografiskTilknytning,
                    adresseForslag,
                    navEnhet,
                    valgtEnhetNr
                )
            }
        }
        return navEnhetFrontendListe.stream().distinct().collect(Collectors.toList())
    }

    private fun addToNavEnhetFrontendListe(
        navEnhetFrontendListe: MutableList<NavEnhetFrontend>,
        geografiskTilknytning: String,
        adresseForslag: AdresseForslag,
        navEnhet: NavEnhet?,
        valgtEnhetNr: String?
    ) {
        val navEnhetFrontend = mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(
            geografiskTilknytning,
            adresseForslag,
            navEnhet,
            valgtEnhetNr
        )
        if (navEnhetFrontend != null) {
            navEnhetFrontendListe.add(navEnhetFrontend)
        }
    }

    private fun mapFraAdresseForslagOgNavEnhetTilNavEnhetFrontend(
        geografiskTilknytning: String,
        adresseForslag: AdresseForslag,
        navEnhet: NavEnhet?,
        valgtEnhetNr: String?
    ): NavEnhetFrontend? {
        if (navEnhet == null) {
            log.warn("Kunne ikke hente NAV-enhet: $geografiskTilknytning , i kommune: ${adresseForslag.kommunenavn} (${adresseForslag.kommunenummer})")
            return null
        }
        var kommunenummer = adresseForslag.kommunenummer
        if (kommunenummer == null || kommunenummer.length != 4) {
            log.warn("Kommunenummer hadde ikke 4 tegn, var $kommunenummer")
            return null
        }
        if (ServiceUtils.isNonProduction() && MockUtils.isAlltidHentKommuneInfoFraNavTestkommune()) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD")
            kommunenummer = "3002"
        }
        val digisosKommune = isDigisosKommune(kommunenummer)
        val sosialOrgnr = if (digisosKommune) navEnhet.sosialOrgNr else null
        val enhetNr = if (digisosKommune) navEnhet.enhetNr else null
        val valgt = enhetNr != null && enhetNr == valgtEnhetNr
        val kommunenavnFraAdresseforslag =
            if (adresseForslag.kommunenavn != null) adresseForslag.kommunenavn else navEnhet.kommunenavn!!
        return NavEnhetFrontend(
            enhetsnr = enhetNr,
            enhetsnavn = navEnhet.navn,
            kommuneNr = kommunenummer,
            kommunenavn = kommuneInfoService.getBehandlingskommune(kommunenummer, kommunenavnFraAdresseforslag),
            orgnr = sosialOrgnr,
            valgt = valgt,
            isMottakDeaktivert = !digisosKommune,
            isMottakMidlertidigDeaktivert = kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer)
        )
    }

    private fun getKommunenummer(oppholdsadresse: JsonAdresse): String? {
        var kommunenummer: String? = null
        when (oppholdsadresse) {
            is JsonMatrikkelAdresse -> kommunenummer = oppholdsadresse.kommunenummer
            is JsonGateAdresse -> kommunenummer = oppholdsadresse.kommunenummer
        }
        return kommunenummer
    }

    private fun isDigisosKommune(kommunenummer: String): Boolean {
        val isNyDigisosApiKommuneMedMottakAktivert =
            kommuneInfoService.kanMottaSoknader(kommunenummer) && ServiceUtils.isSendingTilFiksEnabled()
        val isGammelSvarUtKommune = KommuneTilNavEnhetMapper.getDigisoskommuner().contains(kommunenummer)
        return isNyDigisosApiKommuneMedMottakAktivert || isGammelSvarUtKommune
    }

    private fun getGeografiskTilknytningFromAdresseForslag(adresseForslag: AdresseForslag): String {
        return if (BydelService.BYDEL_MARKA == adresseForslag.geografiskTilknytning) {
            bydelService.getBydelTilForMarka(adresseForslag)
        } else {
            // flere special cases her?
            adresseForslag.geografiskTilknytning
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(NavEnhetRessurs::class.java)
        private const val SPLITTER: String = ", "
    }
}
