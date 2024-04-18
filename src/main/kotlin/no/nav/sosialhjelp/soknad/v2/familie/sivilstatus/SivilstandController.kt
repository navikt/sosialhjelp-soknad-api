package no.nav.sosialhjelp.soknad.v2.familie.sivilstatus

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleDto
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieService
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
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
@RequestMapping("/soknad/{soknadId}/familie/sivilstatus", produces = [MediaType.APPLICATION_JSON_VALUE])
class SivilstandController(private val familieService: FamilieService) {
    @GetMapping
    fun getSivilstand(
        @PathVariable("soknadId") soknadId: UUID,
    ): SivilstandDto? = familieService.findFamilie(soknadId)?.toSivilstandDto()

    @PutMapping
    fun updateSivilstand(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody sivilstandInput: SivilstandInput,
    ): ResponseEntity<SivilstandDto> {
        if (sivilstandInput.sivilstatus != Sivilstatus.GIFT) {
            require(sivilstandInput.ektefelle == null) { "Kan ikke sette ektefelle når man har valgt noe annet enn sivilstatus gift" }
        }
        val updated = familieService.updateSivilstand(soknadId, sivilstandInput.sivilstatus, sivilstandInput.ektefelle?.toDomain())
        return ResponseEntity.ok(updated.toSivilstandDto())
    }
}

data class SivilstandInput(
    val sivilstatus: Sivilstatus?,
    val ektefelle: EktefelleInput?,
)

data class SivilstandDto(
    val sivilstatus: Sivilstatus?,
    val ektefelle: EktefelleDto?,
)

fun Familie.toSivilstandDto() =
    SivilstandDto(
        this.sivilstatus,
        if (this.sivilstatus == Sivilstatus.GIFT) this.ektefelle?.toDto() else null,
    )
