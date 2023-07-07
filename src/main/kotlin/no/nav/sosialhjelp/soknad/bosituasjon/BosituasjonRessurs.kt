package no.nav.sosialhjelp.soknad.bosituasjon

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/soknader/{behandlingsId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GetMapping
    fun hentBosituasjon(@PathVariable("behandlingsId") behandlingsId: String?): BosituasjonFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val bosituasjon = soknad.soknad.data.bosituasjon
        return BosituasjonFrontend(bosituasjon.botype, bosituasjon.antallPersoner)
    }

    @PutMapping
    fun updateBosituasjon(
        @PathVariable("behandlingsId") behandlingsId: String?,
        @RequestBody bosituasjonFrontend: BosituasjonFrontend
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val bosituasjon = jsonInternalSoknad.soknad.data.bosituasjon
        bosituasjon.kilde = JsonKildeBruker.BRUKER
        if (bosituasjonFrontend.botype != null) {
            bosituasjon.botype = bosituasjonFrontend.botype
        }
        bosituasjon.antallPersoner = bosituasjonFrontend.antallPersoner
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier, "updateBosituasjon")
    }

    data class BosituasjonFrontend(
        var botype: Botype?,
        var antallPersoner: Int?
    )
}
