package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataService
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
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
@RequestMapping("/soknad/{soknadId}/personalia/telefonnummer", produces = [MediaType.APPLICATION_JSON_VALUE])
class TelefonnummerController(
    private val soknadService: SoknadService,
    private val brukerdataService: BrukerdataService
) {
    @GetMapping
    fun getTelefonnummer(
        @PathVariable("soknadId") soknadId: UUID
    ): TelefonnummerDto {
        val telefonRegister = soknadService.getSoknad(soknadId).eier.telefonnummer
        val telefonBruker = brukerdataService.getBrukerdataPersonlig(soknadId)?.telefonnummer

        return TelefonnummerDto(
            telefonnummerRegister = telefonRegister,
            telefonnummerBruker = telefonBruker
        )
    }

    @PutMapping
    fun updateTelefon(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) telefonnummerInput: TelefonnummerInput
    ): TelefonnummerDto {
        SoknadInputValidator(TelefonnummerInput::class)
            .validateIsNumber(soknadId, telefonnummerInput.telefonnummerBruker)
        // TODO Validere gyldig telefonnummer ?
        val brukerdata = brukerdataService.updateTelefonnummer(soknadId, telefonnummerInput.telefonnummerBruker)

        return TelefonnummerDto(
            telefonnummerRegister = soknadService.getSoknad(soknadId).eier.telefonnummer,
            telefonnummerBruker = brukerdata.telefonnummer
        )
    }
}

data class TelefonnummerInput(
    val telefonnummerBruker: String
)

data class TelefonnummerDto(
    val telefonnummerRegister: String?,
    val telefonnummerBruker: String?
)
