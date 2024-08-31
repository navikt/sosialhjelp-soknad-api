package no.nav.sosialhjelp.soknad.v2

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

/**
 * En abstraksjon for å skille på logikk som håndterer omkringliggende ting ved en søknad og logikk
 * som direkte opererer/muterer data.
 */
@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad", produces = [MediaType.APPLICATION_JSON_VALUE])
class SoknadLifecycleController(
    private val soknadLifecycleService: SoknadLifecycleService,
    private val nedetidService: NedetidService,
) {
    @PostMapping("/create")
    fun createSoknad(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) token: String,
        response: HttpServletResponse,
    ): StartSoknadResponseDto {
        // TODO bør ikke dette sjekkes ved alle kall? ergo = Interceptor-mat ?
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException(
                "Soknaden har nedetid fram til ${nedetidService.nedetidSluttAsString}",
            )
        }

        return soknadLifecycleService
            .startSoknad(token)
            .let { (id, useKortSoknad) ->
                response.addCookie(xsrfCookie(id.toString()))
                response.addCookie(xsrfCookieMedBehandlingsid(id.toString()))

                StartSoknadResponseDto(id.toString(), useKortSoknad)
            }
    }

    @PostMapping("/{soknadId}/send")
    fun sendSoknad(
        @PathVariable("soknadId") soknadId: UUID,
    ): SoknadSendtDto {
        if (nedetidService.isInnenforNedetid) {
            throw SoknadenHarNedetidException("Soknaden har planlagt nedetid frem til ${nedetidService.nedetidSluttAsString}")
        }
        val (digisosId, innsendingstidspunkt) = soknadLifecycleService.sendSoknad(soknadId)

        return SoknadSendtDto(digisosId, innsendingstidspunkt)
    }

    @DeleteMapping("/{soknadId}/delete")
    fun deleteSoknad(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestHeader(value = HttpHeaders.REFERER) referer: String?,
    ) {
        soknadLifecycleService.cancelSoknad(soknadId = soknadId, referer)
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

data class StartSoknadResponseDto(
    val soknadId: String,
    val useKortSoknad: Boolean,
)

data class SoknadSendtDto(
    val digisosId: UUID,
    val tidspunkt: LocalDateTime,
)
