package no.nav.sosialhjelp.soknad.navenhet

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.AdresseRessurs
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken as eier

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknader/{behandlingsId}/personalia", produces = [MediaType.APPLICATION_JSON_VALUE])
class NavEnhetRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val adresseRessurs: AdresseRessurs
) {
    @PutMapping("/navEnheter")
    fun putNavEnhet(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody navEnhetFrontend: NavEnhetFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = eier()
        val soknad = fetchSoknad(behandlingsId)
        adresseRessurs.setSoknadMottaker(internalFraSoknad(soknad), navEnhetFrontend)
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
    }

    /** Snarvei for SoknadUnderArbeid fra behandlingsId. Sjekker aktiv brukers eierskap. Disse forsvinner igjen i neste commit */
    private fun fetchSoknad(behandlingsId: String): SoknadUnderArbeid = soknadUnderArbeidRepository.hentSoknad(
        behandlingsId,
        eier()
    )

    /** Snarvei for å hente it jsonInternalSoknad fra SoknadUnderArbeid og bekrefte at den ikke er null.  Disse forsvinner igjen i neste commit */
    private fun internalFraSoknad(soknad: SoknadUnderArbeid): JsonInternalSoknad =
        soknad.jsonInternalSoknad ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
}
