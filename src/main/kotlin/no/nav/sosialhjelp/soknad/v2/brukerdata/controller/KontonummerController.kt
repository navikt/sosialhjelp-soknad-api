package no.nav.sosialhjelp.soknad.v2.brukerdata.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.sosialhjelp.soknad.v2.NotValidInputException
import no.nav.sosialhjelp.soknad.v2.SoknadInputValidator
import no.nav.sosialhjelp.soknad.v2.brukerdata.BrukerdataService
import no.nav.sosialhjelp.soknad.v2.brukerdata.KontoInformasjonBruker
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
// @ProtectionSelvbetjeningHigh
@Unprotected
@RequestMapping("/soknad/{soknadId}/personalia/kontonummer")
class KontonummerController(
    private val soknadService: SoknadService,
    private val brukerdataService: BrukerdataService
) {
    @GetMapping
    fun getKontonummer(
        @PathVariable("soknadId") soknadId: UUID
    ): KontoInformasjonDto {
        val kontonummerRegister = soknadService.getSoknad(soknadId).eier.kontonummer
        val kontoInformasjonBruker = brukerdataService.getBrukerdataPersonlig(soknadId)?.kontoInformasjon

        return KontoInformasjonDto(
            harIkkeKonto = kontoInformasjonBruker?.harIkkeKonto,
            kontonummerRegister = kontonummerRegister,
            kontonummerBruker = kontoInformasjonBruker?.kontonummer
        )
    }

    @PutMapping
    fun updateKontoInformasjonBruker(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: KontoInformasjonInput
    ): KontoInformasjonDto {
        SoknadInputValidator(KontoInformasjonInput::class)
            .validateInputNotNullOrEmpty(soknadId, input.harIkkeKonto, input.kontonummerBruker)

        val kontonummerRegister = soknadService.getSoknad(soknadId).eier.kontonummer
        validate(soknadId, input, kontonummerRegister)

        val kontoInformasjon = brukerdataService.updateKontoinformasjon(
            soknadId = soknadId,
            KontoInformasjonBruker(
                kontonummer = input.kontonummerBruker,
                harIkkeKonto = input.harIkkeKonto
            )
        )

        return KontoInformasjonDto(
            harIkkeKonto = kontoInformasjon.harIkkeKonto,
            kontonummerRegister = kontonummerRegister,
            kontonummerBruker = kontoInformasjon.kontonummer
        )
    }

    private fun validate(id: UUID, input: KontoInformasjonInput, kontonummerRegister: String?) {
        if (input.harIkkeKonto == true && (input.kontonummerBruker != null || kontonummerRegister != null)) {
            throw NotValidInputException(id, "HarIkkeKonto er satt, men det finnes kontonummer.")
        }

        if (input.harIkkeKonto == false && input.kontonummerBruker == null && kontonummerRegister == null) {
            throw NotValidInputException(id, "HarIkkeKonto er ikke satt, men det finnes ikke kontonummer")
        }
    }
}

data class KontoInformasjonDto(
    val harIkkeKonto: Boolean?,
    val kontonummerRegister: String?,
    val kontonummerBruker: String?
)

data class KontoInformasjonInput(
    val harIkkeKonto: Boolean?,
    val kontonummerBruker: String?
)
