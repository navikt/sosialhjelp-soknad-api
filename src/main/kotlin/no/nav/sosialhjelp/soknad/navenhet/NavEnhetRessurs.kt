package no.nav.sosialhjelp.soknad.navenhet

import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.MiljoUtils.isNonProduction
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper.getOrganisasjonsnummer
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.INNSENDING_DIGISOSAPI_ENABLED
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService.Companion.BYDEL_MARKA_OSLO
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/soknader/{behandlingsId}/personalia", produces = [MediaType.APPLICATION_JSON_VALUE])
open class NavEnhetRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetService: NavEnhetService,
    private val kommuneInfoService: KommuneInfoService,
    private val bydelFordelingService: BydelFordelingService,
    private val finnAdresseService: FinnAdresseService,
    private val geografiskTilknytningService: GeografiskTilknytningService,
    private val kodeverkService: KodeverkService,
    private val unleash: Unleash,
) {

    @GetMapping("/navEnheter")
    open fun hentNavEnheter(
        @PathVariable("behandlingsId") behandlingsId: String
    ): List<NavEnhetFrontend>? {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad?.soknad
            ?: throw IllegalStateException("Kan ikke hente navEnheter hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val valgtEnhetNr = soknad.mottaker.enhetsnummer
        val oppholdsadresse = soknad.data.personalia.oppholdsadresse
        val adresseValg = utledAdresseValg(oppholdsadresse)
        val navEnhetFrontend = findSoknadsmottaker(eier, soknad, adresseValg, valgtEnhetNr)
        return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
    }

    @GetMapping("/navEnhet")
    open fun hentValgtNavEnhet(
        @PathVariable("behandlingsId") behandlingsId: String
    ): NavEnhetFrontend? {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadsmottaker = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad?.soknad?.mottaker
            ?: throw IllegalStateException("Kan ikke hente valgtNavEnhet hvis SoknadUnderArbeid.jsonInternalSoknad er null")
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
                orgnr = getOrganisasjonsnummer(soknadsmottaker.enhetsnummer), // Brukes ikke etter at kommunene er på Fiks konfigurasjon og burde ikke bli brukt av frontend.
                valgt = true
            )
        }
    }

    @PutMapping("/navEnheter")
    open fun updateNavEnhet(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody navEnhetFrontend: NavEnhetFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        soknad.jsonInternalSoknad?.mottaker = no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withOrganisasjonsnummer(navEnhetFrontend.orgnr)
        soknad.jsonInternalSoknad?.soknad?.mottaker = JsonSoknadsmottaker()
            .withNavEnhetsnavn(createNavEnhetsnavn(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn))
            .withEnhetsnummer(navEnhetFrontend.enhetsnr)
            .withKommunenummer(navEnhetFrontend.kommuneNr)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
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

    private fun createNavEnhetsnavn(enhetsnavn: String, kommunenavn: String?): String {
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
    ): NavEnhetFrontend? {
        val personalia = soknad.data.personalia
        return if ("folkeregistrert" == valg) {
            try {
                finnNavEnhetFraGT(eier, personalia, valgtEnhetNr)
            } catch (e: Exception) {
                log.warn("Noe feilet henting av NavEnhet fra GT -> fallback til adressesøk for vegadresse / hentAdresse for matrikkeladresse", e)
                finnNavEnhetFraAdresse(personalia, valg, valgtEnhetNr)
            }
        } else finnNavEnhetFraAdresse(personalia, valg, valgtEnhetNr)
    }

    private fun finnNavEnhetFraGT(
        ident: String,
        personalia: JsonPersonalia,
        valgtEnhetNr: String?
    ): NavEnhetFrontend? {
        val kommunenummer = getKommunenummer(personalia.oppholdsadresse) ?: return null
        val geografiskTilknytning = geografiskTilknytningService.hentGeografiskTilknytning(ident)
        val navEnhet = navEnhetService.getEnhetForGt(geografiskTilknytning)
        return mapToNavEnhetFrontend(navEnhet, geografiskTilknytning, kommunenummer, valgtEnhetNr)
    }

    private fun finnNavEnhetFraAdresse(
        personalia: JsonPersonalia,
        valg: String?,
        valgtEnhetNr: String?
    ): NavEnhetFrontend? {
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, valg) ?: return null
        val geografiskTilknytning = getGeografiskTilknytningFromAdresseForslag(adresseForslag)
        val navEnhet = navEnhetService.getEnhetForGt(geografiskTilknytning)
        return mapToNavEnhetFrontend(navEnhet, geografiskTilknytning, adresseForslag.kommunenummer, valgtEnhetNr)
    }

    private fun mapToNavEnhetFrontend(
        navEnhet: NavEnhet?,
        geografiskTilknytning: String?,
        kommunenummer: String?,
        valgtEnhetNr: String?
    ): NavEnhetFrontend? {
        if (navEnhet == null) {
            log.warn("Kunne ikke hente NAV-enhet: $geografiskTilknytning , i kommune: $kommunenummer")
            return null
        }
        if (kommunenummer == null || kommunenummer.length != 4) {
            log.warn("Kommunenummer hadde ikke 4 tegn, var $kommunenummer")
            return null
        }
        val isDigisosKommune = isDigisosKommune(kommunenummer)
        val sosialOrgnr = if (isDigisosKommune) navEnhet.sosialOrgNr else null
        val enhetNr = if (isDigisosKommune) navEnhet.enhetNr else null
        val valgt = enhetNr != null && enhetNr == valgtEnhetNr
        val kommunenavn = kodeverkService.getKommunenavn(kommunenummer)
        return NavEnhetFrontend(
            enhetsnr = enhetNr,
            enhetsnavn = navEnhet.navn,
            kommunenavn = kommuneInfoService.getBehandlingskommune(kommunenummer, kommunenavn),
            orgnr = sosialOrgnr,
            valgt = valgt,
            kommuneNr = kommunenummer,
            isMottakDeaktivert = !isDigisosKommune,
            isMottakMidlertidigDeaktivert = kommuneInfoService.harMidlertidigDeaktivertMottak(kommunenummer)
        )
    }

    private fun getKommunenummer(oppholdsadresse: JsonAdresse): String? {
        if (isNonProduction() && unleash.isEnabled(FEATURE_SEND_TIL_NAV_TESTKOMMUNE, false) && isAdresseValgFolkeregistrert(oppholdsadresse)) {
            log.error("Sender til Nav-testkommune (3002). Du skal aldri se denne meldingen i PROD")
            return "3002"
        }

        return when (oppholdsadresse) {
            is JsonMatrikkelAdresse -> oppholdsadresse.kommunenummer
            is JsonGateAdresse -> oppholdsadresse.kommunenummer
            else -> null
        }
    }

    private fun isDigisosKommune(kommunenummer: String): Boolean {
        val isNyDigisosApiKommuneMedMottakAktivert = kommuneInfoService.kanMottaSoknader(kommunenummer) && unleash.isEnabled(INNSENDING_DIGISOSAPI_ENABLED, true)
        val isGammelSvarUtKommune = KommuneTilNavEnhetMapper.digisoskommuner.contains(kommunenummer)
        return isNyDigisosApiKommuneMedMottakAktivert || isGammelSvarUtKommune
    }

    private fun getGeografiskTilknytningFromAdresseForslag(adresseForslag: AdresseForslag): String? {
        return if (BYDEL_MARKA_OSLO == adresseForslag.geografiskTilknytning) {
            bydelFordelingService.getBydelTilForMarka(adresseForslag)
        } else {
            // flere special cases her?
            adresseForslag.geografiskTilknytning
        }
    }

    private fun isAdresseValgFolkeregistrert(adresse: JsonAdresse): Boolean {
        return adresse.adresseValg == JsonAdresseValg.FOLKEREGISTRERT
    }

    companion object {
        private val log = LoggerFactory.getLogger(NavEnhetRessurs::class.java)
        private const val SPLITTER: String = ", "
        const val FEATURE_SEND_TIL_NAV_TESTKOMMUNE = "sosialhjelp.soknad.send-til-nav-testkommune"
    }
}
