package no.nav.sosialhjelp.soknad.v2.familie

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
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
import java.util.UUID

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

        // TODO Ekstra logging
        logger.info("Oppdaterer forsørgerplikt @ Controller med Barnebidrag: ${forsorgerInput.barnebidrag}")

        return forsorgerService
            .updateForsorger(
                soknadId = soknadId,
                barnebidrag = forsorgerInput.barnebidrag,
                updated = forsorgerInput.ansvar.associate { (it.uuid ?: UUID.randomUUID()) to it.toBarn() },
            )
            .toForsorgerDto()
    }

    companion object {
        private val logger by logger()
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
    val samvarsgrad: Int? = null,
    val folkeregistrertSammen: Boolean? = null,
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
    ForsorgerDto(harForsorgerplikt, barnebidrag = barnebidrag, ansvar = ansvar.mapToBarnDtoList())

private fun Map<UUID, Barn>.mapToBarnDtoList(): List<BarnDto> {
    return entries.map { (key, barn) ->
        BarnDto(
            uuid = key,
            barn.navn,
            barn.fodselsdato,
            barn.borSammen,
            barn.folkeregistrertSammen,
            barn.deltBosted,
            barn.samvarsgrad,
        )
    }
}

fun BarnInput.toBarn() = Barn(personId, deltBosted = deltBosted, samvarsgrad = samvarsgrad)
