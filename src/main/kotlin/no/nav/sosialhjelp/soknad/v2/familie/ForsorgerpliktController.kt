package no.nav.sosialhjelp.soknad.v2.familie

import java.util.UUID
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.familie.service.Forsorger
import no.nav.sosialhjelp.soknad.v2.familie.service.ForsorgerService
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/familie/forsorgerplikt", produces = [MediaType.APPLICATION_JSON_VALUE])
class ForsorgerpliktController(private val forsorgerService: ForsorgerService) {
    @GetMapping
    fun getForsorgerplikt(
        @PathVariable soknadId: UUID,
    ) = forsorgerService.findForsorger(soknadId)?.toForsorgerDto() ?: ForsorgerDto()

    @PutMapping
    fun updateForsorgerplikt(
        @PathVariable soknadId: UUID,
        @RequestBody forsorgerInput: ForsorgerInput,
    ): ForsorgerDto {
        require(forsorgerInput.ansvar.isNotEmpty()) { "Ansvar kan ikke være en tom liste" }

        return forsorgerService
            .updateForsorger(
                soknadId = soknadId,
                barnebidrag = forsorgerInput.barnebidrag,
                updated = forsorgerInput.ansvar.map { it.toDomain() },
            )
            .toForsorgerDto()
    }
}

data class ForsorgerInput(
    val barnebidrag: Barnebidrag?,
    val ansvar: List<BarnInput> = emptyList(),
)

data class ForsorgerDto(
    val harForsorgerplikt: Boolean? = null,
    val barnebidrag: Barnebidrag? = null,
    val ansvar: List<BarnDto> = emptyList(),
)

data class BarnInput(
    val uuid: UUID?,
    val personId: String? = null,
    val deltBosted: Boolean? = null,
)

data class BarnDto(
    val uuid: UUID,
    val navn: Navn?,
    val fodselsdato: String?,
    val borSammen: Boolean? = null,
    val folkeregistrertSammen: Boolean? = null,
    val deltBosted: Boolean? = null,
    val samvarsgrad: Int? = null,
)

private fun Forsorger.toForsorgerDto(): ForsorgerDto =
    ForsorgerDto(harForsorgerplikt, barnebidrag = barnebidrag, ansvar = ansvar.values.map(Barn::toDto))

fun BarnInput.toDomain() = Barn(uuid ?: UUID.randomUUID(), personId, deltBosted = deltBosted)

fun Barn.toDto() = BarnDto(familieKey, navn, fodselsdato, borSammen, folkeregistrertSammen, deltBosted, samvarsgrad)
