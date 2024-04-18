package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/begrunnelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class BegrunnelseController(
    private val soknadService: SoknadService,
) {
    @GetMapping
    fun getBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
    ): BegrunnelseDto? {
        // TODO hva skal vi egentlig returnere når bruker ikke har fylt ut data? null, objekt med null-verdier eller 404?
        return soknadService.getSoknad(soknadId).begrunnelse?.toBegrunnelseDto()
            ?: BegrunnelseDto()
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) begrunnelseDto: BegrunnelseDto,
    ): BegrunnelseDto {
        val brukerdata =
            begrunnelseDto.let {
                soknadService.updateBegrunnelse(
                    soknadId = soknadId,
                    begrunnelse =
                        Begrunnelse(
                            hvorforSoke = it.hvorforSoke,
                            hvaSokesOm = it.hvaSokesOm,
                        ),
                )
            }
        return brukerdata.toBegrunnelseDto()
    }
}

data class BegrunnelseDto(
    val hvaSokesOm: String = "",
    val hvorforSoke: String = "",
)

fun Begrunnelse.toBegrunnelseDto(): BegrunnelseDto {
    return BegrunnelseDto(
        hvorforSoke = hvorforSoke,
        hvaSokesOm = hvaSokesOm,
    )
}
