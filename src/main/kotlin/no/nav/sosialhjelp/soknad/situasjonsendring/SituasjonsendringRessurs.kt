package no.nav.sosialhjelp.soknad.situasjonsendring

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde.BRUKER
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.situasjonendring.JsonSituasjonendring
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.shadow.ControllerAdapter
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class SituasjonsendringFrontend(
    val endring: Boolean? = null,
    val hvaErEndret: String? = null,
)

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/situasjonsendring", produces = [APPLICATION_JSON_VALUE])
class SituasjonsendringRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository,
    private val controllerAdapter: ControllerAdapter,
) {
    @GetMapping
    fun hentSituasjonsendring(
        @PathVariable behandlingsId: String,
    ): SituasjonsendringFrontend {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad =
            soknad.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        return jsonInternalSoknad.soknad.data.situasjonendring?.let {
            SituasjonsendringFrontend(it.harNoeEndretSeg, it.hvaHarEndretSeg)
        } ?: SituasjonsendringFrontend()
    }

    @PutMapping
    fun updateSituasjonsendring(
        @PathVariable behandlingsId: String,
        @RequestBody situasjonsendring: SituasjonsendringFrontend,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        requireNotNull(situasjonsendring.endring) { "Endring kan ikke være null" }
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad =
            soknad.jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        if (jsonInternalSoknad.soknad.data.situasjonendring == null) {
            jsonInternalSoknad.soknad.data.situasjonendring = JsonSituasjonendring()
        }

        with(jsonInternalSoknad.soknad.data.situasjonendring) {
            harNoeEndretSeg = situasjonsendring.endring
            hvaHarEndretSeg = situasjonsendring.hvaErEndret
            kilde = JsonKildeBruker.BRUKER
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)

        controllerAdapter.updateSituasjonsendring(behandlingsId, situasjonsendring)
    }
}
