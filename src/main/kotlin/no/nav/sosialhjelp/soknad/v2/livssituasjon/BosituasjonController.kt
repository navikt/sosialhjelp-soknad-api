package no.nav.sosialhjelp.soknad.v2.livssituasjon

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
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
@RequestMapping("/soknad/{soknadId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonController(
    private val livssituasjonService: LivssituasjonService,
) {
    @GetMapping
    fun getBosituasjon(
        @PathVariable("soknadId") soknadId: UUID,
    ): BosituasjonDto {
        return livssituasjonService.getLivssituasjon(soknadId)?.bosituasjon?.toBosituasjonDto()
            ?: BosituasjonDto()
    }

    @PutMapping
    fun updateBosituasjon(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody bosituasjonDto: BosituasjonDto,
    ): BosituasjonDto {
        SoknadInputValidator(BosituasjonDto::class)
            .validateAllInputNotNullOrEmpty(soknadId, bosituasjonDto.botype, bosituasjonDto.antallPersoner)

        return livssituasjonService.updateBosituasjon(
            soknadId,
            botype = bosituasjonDto.botype,
            antallHusstand = bosituasjonDto.antallPersoner,
        ).toBosituasjonDto()
    }
}

data class BosituasjonDto(
    val botype: Botype? = null,
    val antallPersoner: Int? = null,
)

fun Bosituasjon.toBosituasjonDto() =
    BosituasjonDto(
        botype = this.botype,
        antallPersoner = this.antallHusstand,
    )
