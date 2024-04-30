package no.nav.sosialhjelp.soknad.v2.kontakt

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import no.nav.sosialhjelp.soknad.v2.kontakt.service.TelefonService
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
@RequestMapping("/soknad/{soknadId}/personalia/telefonnummer", produces = [MediaType.APPLICATION_JSON_VALUE])
class TelefonnummerController(
    private val telefonService: TelefonService,
) {
    @GetMapping
    fun getTelefonnummer(
        @PathVariable("soknadId") soknadId: UUID,
    ): TelefonnummerDto {
        return telefonService.findTelefonInfo(soknadId)
            ?.let {
                TelefonnummerDto(
                    telefonnummerRegister = it.fraRegister,
                    telefonnummerBruker = it.fraBruker,
                )
            } ?: TelefonnummerDto()
    }

    @PutMapping
    fun updateTelefonnummer(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) telefonnummerInput: TelefonnummerInput,
    ): TelefonnummerDto {
        telefonnummerInput.telefonnummerBruker?.let {
            SoknadInputValidator(TelefonnummerInput::class)
                .validateIsNumber(soknadId, it)
        }

        return telefonService.updateTelefonnummer(soknadId, telefonnummerInput.telefonnummerBruker).let {
            TelefonnummerDto(
                telefonnummerRegister = it.fraRegister,
                telefonnummerBruker = it.fraBruker,
            )
        }
    }
}

data class TelefonnummerInput(
    val telefonnummerBruker: String? = null,
)

data class TelefonnummerDto(
    val telefonnummerRegister: String? = null,
    val telefonnummerBruker: String? = null,
)
