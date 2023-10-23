package no.nav.sosialhjelp.soknad.begrunnelse

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknader/{behandlingsId}/begrunnelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class BegrunnelseRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository
) {
    @GetMapping
    fun hentBegrunnelse(
        @PathVariable("behandlingsId") behandlingsId: String
    ): BegrunnelseFrontend {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        return getBegrunnelseFromSoknad(behandlingsId);
    }

    private fun getBegrunnelseFromSoknad(behandlingsId: String) :BegrunnelseFrontend {
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).jsonInternalSoknad
                ?: throw IllegalStateException("Kan ikke hente søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val begrunnelse = soknad.soknad.data.begrunnelse
        return BegrunnelseFrontend(begrunnelse.hvaSokesOm, begrunnelse.hvorforSoke)
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody begrunnelseFrontend: BegrunnelseFrontend
    ): BegrunnelseFrontend {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val begrunnelse = jsonInternalSoknad.soknad.data.begrunnelse
        begrunnelse.kilde = JsonKildeBruker.BRUKER
        begrunnelse.hvaSokesOm = begrunnelseFrontend.hvaSokesOm
        begrunnelse.hvorforSoke = begrunnelseFrontend.hvorforSoke
        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
        return getBegrunnelseFromSoknad(behandlingsId);
    }

    data class BegrunnelseFrontend(
        val hvaSokesOm: String?,
        val hvorforSoke: String?
    )
}
