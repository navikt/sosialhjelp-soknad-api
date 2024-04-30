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
import no.nav.sosialhjelp.soknad.v2.soknad.service.SoknadServiceImpl

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/begrunnelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class BegrunnelseController(
    private val soknadServiceImpl: SoknadServiceImpl,
) {
    @GetMapping
    fun getBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
    ): BegrunnelseDto {
        return soknadServiceImpl.findSoknad(soknadId).begrunnelse.toBegrunnelseDto()
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) begrunnelseDto: BegrunnelseDto,
    ): BegrunnelseDto {
        val brukerdata =
            begrunnelseDto.let {
                soknadServiceImpl.updateBegrunnelse(
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
