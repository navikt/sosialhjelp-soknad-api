package no.nav.sosialhjelp.soknad.v2.familie.forsorgerplikt

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.BarnDto
import no.nav.sosialhjelp.soknad.v2.familie.BarnInput
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.familie.toDomain
import no.nav.sosialhjelp.soknad.v2.familie.toDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/familie/forsorgerplikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class ForsorgerpliktController(private val familieService: FamilieService) {

    @GetMapping
    fun getForsorgerplikt(@PathVariable soknadId: UUID): ForsorgerDto? = familieService.findFamilie(soknadId)?.toForsorgerDto()

    @PutMapping
    fun updateForsorgerplikt(
        @PathVariable soknadId: UUID,
        @RequestBody forsorgerInput: ForsorgerInput
    ): ResponseEntity<ForsorgerDto> {
        require(forsorgerInput.ansvar.isNotEmpty()) { "Ansvar kan ikke være en tom liste" }

        val updated = familieService.updateForsorger(
            soknadId,
            forsorgerInput.barnebidrag,
            forsorgerInput.ansvar.map { it.toDomain() }
        )
        return ResponseEntity.ok(updated.toForsorgerDto())
    }
}

data class ForsorgerInput(
    val barnebidrag: Barnebidrag?,
    val ansvar: List<BarnInput> = emptyList()
)

data class ForsorgerDto(
    val harForsorgerplikt: Boolean?,
    val barnebidrag: Barnebidrag?,
    val ansvar: List<BarnDto>
)

private fun Familie.toForsorgerDto(): ForsorgerDto = ForsorgerDto(harForsorgerplikt, barnebidrag = barnebidrag, ansvar = ansvar.values.map(Barn::toDto))
