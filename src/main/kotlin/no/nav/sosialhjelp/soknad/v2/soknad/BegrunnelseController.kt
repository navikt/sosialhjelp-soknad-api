package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Unprotected
// @ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknad/{soknadId}/begrunnelse", produces = [MediaType.APPLICATION_JSON_VALUE])
class BegrunnelseController(
    private val soknadService: SoknadService
) {
    @GetMapping
    fun getBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID
    ): BegrunnelseDto? {
        // TODO hva skal vi egentlig returnere når bruker ikke har fylt ut data? null, objekt med null-verdier eller 404?
        return soknadService.getSoknad(soknadId).begrunnelse.toBegrunnelseDto()
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) begrunnelseDto: BegrunnelseDto
    ): BegrunnelseDto {
        begrunnelseDto.validate(soknadId)

        val brukerdata = begrunnelseDto.let {
            soknadService.updateBegrunnelse(
                soknadId = soknadId,
                begrunnelse = Begrunnelse(
                    hvorforSoke = it.hvorforSoke,
                    hvaSokesOm = it.hvaSokesOm
                )
            )
        }
        return brukerdata.toBegrunnelseDto()
    }
}

private fun BegrunnelseDto.validate(soknadId: UUID) {
    SoknadInputValidator(BegrunnelseDto::class).validateAllInputNotNullOrEmpty(soknadId, hvaSokesOm, hvorforSoke)
//    SoknadInputValidator(BegrunnelseDto::class).validateInputStringNotNullOrEmpty(soknadId, hvaSokesOm, hvorforSoke)

    listOfNotNull(hvorforSoke, hvaSokesOm)
        .forEach {
            SoknadInputValidator(BegrunnelseDto::class).validateTextInput(soknadId, it)
        }
}

data class BegrunnelseDto(
    val hvaSokesOm: String? = null,
    val hvorforSoke: String? = null
)

fun Begrunnelse.toBegrunnelseDto(): BegrunnelseDto {
    return BegrunnelseDto(
        hvorforSoke = hvorforSoke,
        hvaSokesOm = hvaSokesOm
    )
}
