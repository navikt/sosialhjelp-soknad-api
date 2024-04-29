package no.nav.sosialhjelp.soknad.v2.soknad

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.eier.Eier
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import no.nav.sosialhjelp.soknad.v2.eier.EierService

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/personalia/basisPersonalia", produces = [MediaType.APPLICATION_JSON_VALUE])
class BasisPersonaliaController(
    private val eierService: EierService,
) {
    fun getBasisPersonalia(
        @PathVariable("soknadId") soknadId: UUID,
    ): PersonaliaDto {
        return eierService.findEier(soknadId).toPersonaliaDto()
    }
}

private fun Eier.toPersonaliaDto(): PersonaliaDto {
    return PersonaliaDto(
        navn =
            NavnDto(
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn,
            ),
        statsborgerskap = statsborgerskap,
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
    val etternavn: String,
)
