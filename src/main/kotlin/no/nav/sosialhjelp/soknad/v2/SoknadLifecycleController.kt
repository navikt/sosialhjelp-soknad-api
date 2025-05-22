package no.nav.sosialhjelp.soknad.v2

import jakarta.servlet.http.HttpServletResponse
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadType
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
    private val soknadLifecycleService: SoknadLifecycleUseCaseHandler,
    private val soknadMetadataService: SoknadMetadataService,
) {
    @PostMapping("/create")
    fun createSoknad(
        @RequestParam(value = "soknadstype", required = false) soknadstype: String?,
        response: HttpServletResponse,
    ): StartSoknadResponseDto {
        val isKort =
            if (MiljoUtils.isNonProduction()) {
                when (soknadstype) {
                    "kort" -> true
                    "standard" -> false
                    else -> false
                }
            } else {
                false
            }

        return soknadLifecycleService
            .startSoknad(isKort)
            .let { soknadId -> StartSoknadResponseDto(soknadId, false) }
    }

    @PostMapping("/{soknadId}/send")
    fun sendSoknad(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) token: String?,
    ): SoknadSendtDto {
        val (digisosId, innsendingstidspunkt) = soknadLifecycleService.sendSoknad(soknadId, token)

        throw AuthorizationException("Ikke tilgang")

//        return SoknadSendtDto(digisosId, innsendingstidspunkt)
    }

    @DeleteMapping("/{soknadId}/delete")
    fun deleteSoknad(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestHeader(value = HttpHeaders.REFERER) referer: String?,
    ) {
        soknadLifecycleService.cancelSoknad(soknadId = soknadId, referer)
    }

    @GetMapping("/{soknadId}/isKort")
    fun isKortSoknad(
        @PathVariable soknadId: String,
    ): Boolean = soknadMetadataService.getSoknadType(UUID.fromString(soknadId)) == SoknadType.KORT
}

data class StartSoknadResponseDto(
    val soknadId: UUID,
    val useKortSoknad: Boolean,
)

data class SoknadSendtDto(
    val digisosId: UUID,
    val tidspunkt: LocalDateTime,
)
