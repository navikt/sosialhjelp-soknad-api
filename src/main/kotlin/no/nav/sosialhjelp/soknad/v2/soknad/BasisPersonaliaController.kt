package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.app.Constants
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Unprotected
//@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4, Constants.CLAIM_ACR_LOA_HIGH], combineWithOr = true)
@RequestMapping("/soknad/{soknadId}/personalia/basisPersonalia", produces = [MediaType.APPLICATION_JSON_VALUE])
class BasisPersonaliaController(
    private val soknadService: SoknadService
) {
    fun getBasisPersonalia(@PathVariable("soknadId") soknadId: UUID): PersonaliaDto {
        return soknadService.getSoknad(soknadId).toPersonaliaDto()
    }
}

private fun Soknad.toPersonaliaDto(): PersonaliaDto {
    return PersonaliaDto(
        navn = NavnDto(
            fornavn = eier.navn.fornavn,
            mellomnavn = eier.navn.mellomnavn,
            etternavn = eier.navn.etternavn
        ),
        statsborgerskap = eier.statsborgerskap
    )
}

data class PersonaliaDto(
    val navn: NavnDto,
    // TODO Nødvendig / riktig å sende med fødselsnummer i denne Dto'en ?
//    val fodselsnummer: String? = null,
    val statsborgerskap: String? = null,
)

data class NavnDto(
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
)
