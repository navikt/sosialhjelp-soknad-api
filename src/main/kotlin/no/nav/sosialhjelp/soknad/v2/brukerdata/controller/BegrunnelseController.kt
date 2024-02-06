package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.brukerdata.Begrunnelse
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataPerson
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataService
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
    ): BegrunnelseDto? {
        // TODO hva skal vi egentlig returnere når bruker ikke har fylt ut data? null, objekt med null-verdier eller 404?
        return brukerdataService.getBrukerdataPersonlig(soknadId)?.toBegrunnelseDto()
    }

    @PutMapping
    fun updateBegrunnelse(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) begrunnelseDto: BegrunnelseDto
    ): BegrunnelseDto {
        begrunnelseDto.validate()

        val brukerdata = begrunnelseDto.let {
            brukerdataService.updateBegrunnelse(
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

private fun BegrunnelseDto.validate() {
    if (hvorforSoke == null && hvaSokesOm == null) {
        throw IllegalArgumentException("Begrunnelse inneholder ikke data.")
    }

    listOfNotNull(hvorforSoke, hvaSokesOm)
        .flatMap { it.toList() }
        .forEach {
            if (!it.isLetterOrDigit() && !it.isWhitespace()) {
                throw IllegalStateException("Kun bokstaver og tall er lovlig i Begrunnelse.")
            }
        }
}

data class BegrunnelseDto(
    val hvaSokesOm: String? = null,
    val hvorforSoke: String? = null
)

fun BrukerdataPerson.toBegrunnelseDto(): BegrunnelseDto {
    begrunnelse.let {
        return BegrunnelseDto(
            hvorforSoke = it?.hvorforSoke,
            hvaSokesOm = it?.hvaSokesOm
        )
    }
}
