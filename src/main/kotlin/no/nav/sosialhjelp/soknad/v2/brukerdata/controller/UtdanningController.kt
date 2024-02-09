package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.NotValidInputException
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataService
import no.nav.sosialhjelp.soknad.v2.brukerdata.Studentgrad
import no.nav.sosialhjelp.soknad.v2.brukerdata.Utdanning
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
@RequestMapping("/soknad/{soknadId}/utdanning", produces = [MediaType.APPLICATION_JSON_VALUE])
class UtdanningController(
    private val brukerdataService: BrukerdataService
) {
    @GetMapping
    fun getUtdanning(@PathVariable("soknadId") soknadId: UUID): UtdanningDto? {
        return brukerdataService.getBrukerdataFormelt(soknadId)?.utdanning?.toUtdanningDto()
    }

    @PutMapping
    fun updateUtdanning(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody utdanningDto: UtdanningDto,
    ): UtdanningDto {
        utdanningDto.validate(soknadId)

        return brukerdataService.updateUtdanning(
            soknadId = soknadId,
            utdanning = Utdanning(
                erStudent = utdanningDto.erStudent,
                studentGrad = utdanningDto.studentgrad,
            )
        ).utdanning!!.toUtdanningDto() // utdanning skal aldri være null på dette tidspunktet
    }
}

private fun UtdanningDto.validate(soknadId: UUID) {
    SoknadInputValidator(UtdanningDto::class)
        .validateAllInputNotNullOrEmpty(soknadId, erStudent, studentgrad)

    if (!erStudent && studentgrad != null) {
        throw NotValidInputException(soknadId, "erStudent er satt, men studentgrad er ikke null.")
    }
}

data class UtdanningDto(
    val erStudent: Boolean,
    val studentgrad: Studentgrad? = null,
)

private fun Utdanning.toUtdanningDto(): UtdanningDto {
    return UtdanningDto(
        erStudent = erStudent,
        studentgrad = studentGrad
    )
}
