package no.nav.sosialhjelp.soknad.innsending

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.Constants
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.innsending.dto.BekreftelseRessurs
import no.nav.sosialhjelp.soknad.innsending.dto.StartSoknadResponse
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
    @GetMapping("/{behandlingsId}/xsrfCookie")
    fun hentXsrfCookie(
        @PathVariable("behandlingsId") behandlingsId: String,
        response: HttpServletResponse,
    ): Boolean {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId)
        response.addCookie(xsrfCookie(behandlingsId))
        response.addCookie(xsrfCookieMedBehandlingsid(behandlingsId))

        soknadHandlerProxy.updateLastChanged(behandlingsId)

        return true
    }

    @GetMapping("/{behandlingsId}/erSystemdataEndret")
    fun sjekkOmSystemdataErEndret(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): Boolean {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return soknadHandlerProxy.isRegisterdataChanged(behandlingsId)
    }

    @PostMapping("/{behandlingsId}/oppdaterSamtykker")
    fun oppdaterSamtykker(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestBody samtykker: List<BekreftelseRessurs>,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ) {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        val harBostotteSamtykke =
            samtykker
                .any { it.type.equals(BOSTOTTE_SAMTYKKE, ignoreCase = true) && it.verdi == true }
        val harSkatteetatenSamtykke =
            samtykker
                .any { it.type.equals(UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true) && it.verdi == true }

        soknadHandlerProxy.updateSamtykker(behandlingsId, harBostotteSamtykke, harSkatteetatenSamtykke)
    }

    @GetMapping("/{behandlingsId}/hentSamtykker")
    fun hentSamtykker(
        @PathVariable("behandlingsId") behandlingsId: String,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): List<BekreftelseRessurs> {
        tilgangskontroll.verifiserAtBrukerHarTilgang()

        return soknadHandlerProxy.getSamtykker(behandlingsId, token)
    }

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

    companion object {
        const val XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API"

        private fun xsrfCookie(behandlingId: String): Cookie {
            val xsrfCookie = Cookie(XSRF_TOKEN, XsrfGenerator.generateXsrfToken(behandlingId))
            xsrfCookie.path = "/"
            xsrfCookie.secure = true
            return xsrfCookie
        }

        private fun xsrfCookieMedBehandlingsid(behandlingId: String): Cookie {
            val xsrfCookie = Cookie("$XSRF_TOKEN-$behandlingId", XsrfGenerator.generateXsrfToken(behandlingId))
            xsrfCookie.path = "/"
            xsrfCookie.secure = true
            return xsrfCookie
        }
    }
}
