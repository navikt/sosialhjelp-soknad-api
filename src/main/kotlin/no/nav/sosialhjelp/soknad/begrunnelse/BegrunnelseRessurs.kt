package no.nav.sosialhjelp.soknad.begrunnelse

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SamtidigOppdateringException
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource.Companion.log
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
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)
        val eier = SubjectHandlerUtils.getUserIdFromToken()
        val soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier)
        val jsonInternalSoknad = soknad.jsonInternalSoknad
            ?: throw IllegalStateException("Kan ikke oppdatere søknaddata hvis SoknadUnderArbeid.jsonInternalSoknad er null")
        val begrunnelse = jsonInternalSoknad.soknad.data.begrunnelse
        begrunnelse.kilde = JsonKildeBruker.BRUKER
        begrunnelse.hvaSokesOm = begrunnelseFrontend.hvaSokesOm
        begrunnelse.hvorforSoke = begrunnelseFrontend.hvorforSoke
        try {
            log.info(
                "${this::class.java.name} - Oppdaterer søknad under arbeid for ${soknad.behandlingsId} - " +
                    "Versjon: ${soknad.versjon}, " +
                    "Sist endret: ${soknad.sistEndretDato}"
            )
            soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier)
            // TODO *** EKSTRA LOGGING
            log.info(
                "${this::class.java.name} - Søknad under arbeid er oppdatert for ${soknad.behandlingsId} " +
                    "Versjon: ${soknad.versjon}, " +
                    "Sist endret: ${soknad.sistEndretDato}"
            )
        } catch (e: SamtidigOppdateringException) {
            log.error("${this::class.java.name} - ${e.message}")
        }
    }

    data class BegrunnelseFrontend(
        val hvaSokesOm: String?,
        val hvorforSoke: String?
    )
}
