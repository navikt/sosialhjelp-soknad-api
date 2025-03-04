package no.nav.sosialhjelp.soknad.innsending

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.innsending.dto.SendTilUrlFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader/{behandlingsId}/actions", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadActions(
    private val tilgangskontroll: Tilgangskontroll,
    private val nedetidService: NedetidService,
    private val sendSoknadProxy: SendSoknadProxy,
) {
    @PostMapping("/send")
    fun sendSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): SendTilUrlFrontend {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}")
        }
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        return sendSoknadProxy.sendSoknad(behandlingsId, token)
    }
}
