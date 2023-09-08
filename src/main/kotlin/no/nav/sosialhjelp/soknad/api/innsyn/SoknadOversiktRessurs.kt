package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.innsyn.dto.SoknadOversiktDto
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknadoversikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadOversiktRessurs(
    private val service: SoknadOversiktService,
    private val tilgangskontroll: Tilgangskontroll
) {
    @GetMapping("/soknader")
    fun hentInnsendteSoknaderForBruker(): List<SoknadOversiktDto> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        logger.debug("Henter alle søknader")
        val soknader: List<SoknadOversiktDto> = service.hentSvarUtSoknaderFor(fnr)
        logger.debug("Hentet ${soknader.size} søknader for bruker")
        return soknader
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SoknadOversiktRessurs::class.java)
    }
}
