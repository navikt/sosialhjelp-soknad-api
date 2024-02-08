package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import no.nav.sosialhjelp.soknad.v2.brukerdata.Bosituasjon
import no.nav.sosialhjelp.soknad.v2.brukerdata.Botype
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
// @ProtectionSelvbetjeningHigh
@Unprotected
@RequestMapping("/soknad/{soknadId}/bosituasjon", produces = [MediaType.APPLICATION_JSON_VALUE])
class BosituasjonController(
    private val brukerdataService: BrukerdataService
) {
    @GetMapping
    fun getBosituasjon(@PathVariable("soknadId") soknadId: UUID): BosituasjonDto {
        return brukerdataService.getBrukerdataPersonlig(soknadId)?.toBosituasjonDto()
            ?: BosituasjonDto()
    }

    @PutMapping
    fun updateBosituasjon(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody bosituasjonDto: BosituasjonDto
    ): BosituasjonDto {

        SoknadInputValidator(BosituasjonDto::class)
            .validateInputNotNullOrEmpty(soknadId, bosituasjonDto.botype, bosituasjonDto.antallPersoner)

        return brukerdataService.updateBosituasjon(
            soknadId,
            Bosituasjon(
                botype = bosituasjonDto.botype,
                antallHusstand = bosituasjonDto.antallPersoner
            )
        ).toBosituasjonDto()
    }
}

data class BosituasjonDto(
    val botype: Botype? = null,
    val antallPersoner: Int? = null
)

private fun BrukerdataPerson.toBosituasjonDto() = BosituasjonDto(
    botype = bosituasjon?.botype,
    antallPersoner = bosituasjon?.antallHusstand,
)
