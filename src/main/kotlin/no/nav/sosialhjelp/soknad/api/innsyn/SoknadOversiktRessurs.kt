package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/soknadoversikt", produces = [MediaType.APPLICATION_JSON_VALUE])
open class SoknadOversiktRessurs(
    private val service: SoknadOversiktService,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GetMapping("/soknader")
    open fun hentInnsendteSoknaderForBruker(): List<SoknadOversiktDto> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        log.debug("Henter alle søknader")
        val soknader: List<SoknadOversiktDto> = service.hentSvarUtSoknaderFor(fnr)
        log.debug("Hentet ${soknader.size} søknader for bruker")
        return soknader
    }

    companion object {
        private val log by logger()
    }
}
