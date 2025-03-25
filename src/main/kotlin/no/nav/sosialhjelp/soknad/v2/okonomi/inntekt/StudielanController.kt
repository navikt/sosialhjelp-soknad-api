package no.nav.sosialhjelp.soknad.v2.okonomi.inntekt

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.livssituasjon.UtdanningService
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
@RequestMapping("/soknad/{soknadId}/inntekt/studielan", produces = [MediaType.APPLICATION_JSON_VALUE])
class StudielanController(
    private val studielanService: StudielanService,
    private val utdanningService: UtdanningService,
) {
    @GetMapping
    fun getHasStudielan(
        @PathVariable("soknadId") soknadId: UUID,
    ): StudielanDto {
        return utdanningService.findUtdanning(soknadId)?.let {
            if (it.erStudent) {
                StudielanDto(erStudent = true, mottarStudielan = studielanService.getHarStudielan(soknadId))
            } else {
                StudielanDto(erStudent = false)
            }
        }
            ?: StudielanDto()
    }

    @PutMapping
    fun updateStudielan(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: StudielanInput,
    ): StudielanDto {
        utdanningService.findUtdanning(soknadId)?.let {
            if (it.erStudent) studielanService.updateStudielan(soknadId, input.mottarStudielan)
        }
        return getHasStudielan(soknadId)
    }
}

data class StudielanDto(
    val erStudent: Boolean? = null,
    val mottarStudielan: Boolean? = null,
)

data class StudielanInput(
    val mottarStudielan: Boolean,
)
