package no.nav.sosialhjelp.soknad.v2.familie

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.familie.service.Sivilstand
import no.nav.sosialhjelp.soknad.v2.familie.service.SivilstandService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
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
@RequestMapping("/soknad/{soknadId}/familie/sivilstatus", produces = [MediaType.APPLICATION_JSON_VALUE])
class SivilstandController(private val sivilstandService: SivilstandService) {
    @GetMapping
    fun getSivilstand(
        @PathVariable("soknadId") soknadId: UUID,
    ): SivilstandDto = sivilstandService.findSivilstand(soknadId)?.toSivilstandDto() ?: SivilstandDto()

    @PutMapping
    fun updateSivilstand(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody sivilstandInput: SivilstandInput,
    ): SivilstandDto {
        if (sivilstandInput.sivilstatus != Sivilstatus.GIFT) {
            require(sivilstandInput.ektefelle == null) { "Kan ikke sette ektefelle når man har valgt noe annet enn sivilstatus gift" }
        }
        return sivilstandService
            .updateSivilstand(soknadId, sivilstandInput.sivilstatus, sivilstandInput.ektefelle?.toDomain())
            .toSivilstandDto()
    }
}

data class EktefelleInput(
    val personId: String?,
    val navn: Navn,
    val fodselsdato: String? = null,
    val borSammen: Boolean? = null,
)

data class SivilstandInput(
    val sivilstatus: Sivilstatus?,
    val ektefelle: EktefelleInput?,
)

data class SivilstandDto(
    val sivilstatus: Sivilstatus? = null,
    val ektefelle: EktefelleDto? = null,
)

data class EktefelleDto(
    val personId: String?,
    val navn: Navn?,
    val fodselsdato: String?,
    val harDiskresjonskode: Boolean? = null,
    val folkeregistrertMedEktefelle: Boolean? = null,
    val borSammen: Boolean? = null,
)

fun Sivilstand.toSivilstandDto() =
    SivilstandDto(
        this.sivilstatus,
        ektefelle?.toDto(),
    )

fun Ektefelle.toDto() = EktefelleDto(personId, navn, fodselsdato, folkeregistrertMedEktefelle, borSammen)

fun EktefelleInput.toDomain() = Ektefelle(navn, fodselsdato, personId, borSammen = borSammen, kildeErSystem = false)
