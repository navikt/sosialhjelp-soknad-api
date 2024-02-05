package no.nav.sosialhjelp.soknad.v2.brukerdata

import no.nav.security.token.support.core.api.Unprotected
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
    private val brukerdataService: BrukerdataService
) {
    @GetMapping
    fun getBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID
    ): BegrunnelseDto {
        val brukerdata = brukerdataService.getBrukerdata(soknadId)
        return brukerdata?.toBegrunnelseDto() ?: BegrunnelseDto()
        // TODO hva skal vi egentlig returnere når bruker ikke har fylt ut data? null, objekt med null-verdier eller 404?
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) begrunnelseDto: BegrunnelseDto
    ): BegrunnelseDto {
        // TODO Validere at tekst kun inneholder bokstaver og tall?

        val brukerdata = begrunnelseDto.let {
            brukerdataService.updateBegrunnelse(
                soknadId = soknadId,
                begrunnelse = Begrunnelse(
                    hvorforSoke = it.hvorforSoke,
                    hvaSokesOm = it.hvaSokesOm
                )
            )
        }
        return brukerdata.toBegrunnelseDto() ?: BegrunnelseDto()
    }
}

data class BegrunnelseDto(
    val hvaSokesOm: String? = null,
    val hvorforSoke: String? = null
)

fun Brukerdata.toBegrunnelseDto(): BegrunnelseDto? {
    return begrunnelse?.let {
        BegrunnelseDto(
            hvorforSoke = it.hvorforSoke,
            hvaSokesOm = it.hvaSokesOm
        )
    }
}
