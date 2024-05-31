package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/inntekt/formue")
class FormueController(
    private val formueService: FormueService,
) {
    @GetMapping
    fun getFormue(
        @PathVariable("soknadId") soknadId: UUID,
    ): FormueDto {
        return formueService.getBeskrivelseSparing(soknadId).let {
            formueService.getFormuer(soknadId).toFormueDto(it)
        }
    }

    @PutMapping
    fun updateFormue(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: FormueInput,
    ): FormueDto {
        return formueService
            .updateFormue(soknadId = soknadId, input = input)
            .toFormueDto(beskrivelseSparing = formueService.getBeskrivelseSparing(soknadId))
    }
}

// TODO Trenger man både flagg og tekststreng for beskrivelse av annet (sparing) ?
data class FormueDto(
    val hasBrukskonto: Boolean = false,
    val hasSparekonto: Boolean = false,
    val hasBsu: Boolean = false,
    val hasLivsforsikring: Boolean = false,
    val hasVerdipapirer: Boolean = false,
    val hasSparing: Boolean = false,
    val beskrivelseSparing: String? = null,
)

private fun List<Formue>.toFormueDto(beskrivelseSparing: String?): FormueDto {
    return FormueDto(
        hasBrukskonto = any { it.type == FormueType.FORMUE_BRUKSKONTO },
        hasSparekonto = any { it.type == FormueType.FORMUE_SPAREKONTO },
        hasBsu = any { it.type == FormueType.FORMUE_BSU },
        hasLivsforsikring = any { it.type == FormueType.FORMUE_LIVSFORSIKRING },
        hasVerdipapirer = any { it.type == FormueType.FORMUE_VERDIPAPIRER },
        hasSparing = beskrivelseSparing != null,
        beskrivelseSparing = beskrivelseSparing,
    )
}

data class FormueInput(
    val hasBrukskonto: Boolean = false,
    val hasSparekonto: Boolean = false,
    val hasBsu: Boolean = false,
    val hasLivsforsikring: Boolean = false,
    val hasVerdipapirer: Boolean = false,
    val beskrivelseSparing: String? = null,
)
