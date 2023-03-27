package no.nav.sosialhjelp.soknad.api.minesaker

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.api.minesaker.dto.InnsendtSoknadDto
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_3
import no.nav.sosialhjelp.soknad.app.Constants.CLAIM_ACR_LEVEL_4
import no.nav.sosialhjelp.soknad.app.Constants.TOKENX
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = TOKENX, combineWithOr = true, claimMap = [CLAIM_ACR_LEVEL_3, CLAIM_ACR_LEVEL_4])
@RequestMapping("/minesaker", produces = [MediaType.APPLICATION_JSON_VALUE])
class MineSakerMetadataRessurs(
    private val mineSakerMetadataService: MineSakerMetadataService
) {
    /**
     * Henter informasjon om innsendte søknader via SoknadMetadataRepository.
     * På sikt vil vi hente denne informasjonen fra Fiks (endepunkt vil da høre mer hjemme i innsyn-api)
     */
    @GetMapping("/innsendte")
    fun hentInnsendteSoknaderForBruker(): List<InnsendtSoknadDto> {
        val fnr = SubjectHandlerUtils.getUserIdFromToken()
        return mineSakerMetadataService.hentInnsendteSoknader(fnr)
    }

    @Unprotected
    @GetMapping("/ping")
    fun ping(): String {
        log.debug("Ping for MineSaker")
        return "pong"
    }

    companion object {
        private val log = LoggerFactory.getLogger(MineSakerMetadataRessurs::class.java)
    }
}
