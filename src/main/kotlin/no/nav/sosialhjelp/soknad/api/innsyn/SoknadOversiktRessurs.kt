package no.nav.sosialhjelp.soknad.api.innsyn

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.TOKENX, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknadoversikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadOversiktRessurs {

    @GetMapping("/soknader")
    fun hentInnsendteSoknaderForBruker(): List<Any> {
        LoggerFactory.getLogger(this::class.java).warn("Det finnes ikke lenger søknader sendt med SvarUt")
        return emptyList()
    }
}
