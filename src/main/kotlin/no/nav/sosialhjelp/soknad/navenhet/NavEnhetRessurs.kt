package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseRessurs
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
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader/{behandlingsId}/personalia", produces = [MediaType.APPLICATION_JSON_VALUE])
class NavEnhetRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val navEnhetService: NavEnhetService,
    private val adresseRessurs: AdresseRessurs
) {

    @Deprecated("Brukes ikke")
    @GetMapping("/navEnheter")
    fun getNavEnheter(
        @PathVariable("behandlingsId") behandlingsId: String
    ): List<NavEnhetFrontend> {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad?.soknad
            ?: throw IllegalStateException("Kan ikke hente navEnheter hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val oppholdsadresse = soknad.data.personalia.oppholdsadresse
        val adresseValg: JsonAdresseValg? = oppholdsadresse?.adresseValg

        // TODO Ekstra logging
        LoggerFactory.getLogger(this::class.java).info("Henter navEnhet path /navEnheter")
        val navEnhetFrontend = navEnhetService.getNavEnhet(eier, soknad, adresseValg)

        return navEnhetFrontend?.let { listOf(it) } ?: emptyList()
    }

    @GetMapping("/navEnhet")
    fun getValgtNavEnhet(
        @PathVariable("behandlingsId") behandlingsId: String
    ): NavEnhetFrontend? {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknadsmottaker = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad?.soknad?.mottaker
            ?: throw IllegalStateException("Kan ikke hente valgtNavEnhet hvis SoknadUnderArbeid.jsonInternalSoknad er null")

        return if (soknadsmottaker.kommunenummer.isNullOrEmpty() || soknadsmottaker.navEnhetsnavn.isNullOrEmpty()) {
            null
        } else {
            navEnhetService.getValgtNavEnhet(soknadsmottaker)
        }
    }

    @PutMapping("/navEnheter")
    fun putNavEnhet(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody navEnhetFrontend: NavEnhetFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)

        // TODO Ekstra logging
        LoggerFactory.getLogger(this::class.java).warn("Setter navEnhet - PUT path /navEnheter")

        adresseRessurs.setNavEnhetAsMottaker(soknad, navEnhetFrontend, eier)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }
}
