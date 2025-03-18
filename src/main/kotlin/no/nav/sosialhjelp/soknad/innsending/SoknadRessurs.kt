package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.HttpServletResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectedWithClaims(
    issuer = Constants.SELVBETJENING,
    claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH],
    combineWithOr = true,
)
@RequestMapping("/soknader", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadRessurs(
    private val tilgangskontroll: Tilgangskontroll,
    private val nedetidService: NedetidService,
    private val soknadHandlerProxy: SoknadHandlerProxy,
) {
    @PostMapping("/opprettSoknad")
    fun opprettSoknad(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
        @RequestParam(value = "soknadstype", required = false) soknadstype: String?,
        response: HttpServletResponse,
    ): StartSoknadResponse {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}")
        }
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return soknadHandlerProxy.createSoknad(soknadstype, response)
    }

    @DeleteMapping("/{behandlingsId}")
    fun slettSoknad(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.REFERER) referer: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId)

        soknadHandlerProxy.cancelSoknad(behandlingsId, referer)
    }

    @GetMapping("/{behandlingsId}/isKort")
    fun isKortSoknad(
        @PathVariable behandlingsId: String,
    ): Boolean {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)

        return soknadHandlerProxy.isKort(UUID.fromString(behandlingsId))
    }
}
