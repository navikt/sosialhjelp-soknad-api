package no.nav.sosialhjelp.soknad.v2.brukerdata

import no.nav.security.token.support.core.api.Unprotected
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
    ): KontoDto {
        val kontonummerRegister = soknadService.getKontonummer(soknadId)
        val kontoInformasjonBruker = brukerdataService.getBrukerdata(soknadId)?.kontoInformasjon

        return KontoDto(
            harIkkeKonto = kontoInformasjonBruker?.harIkkeKonto,
            kontonummerRegister = kontonummerRegister,
            kontonummerBruker = kontoInformasjonBruker?.kontonummer
        )
    }

    @PutMapping
    fun updateKontoInformasjonBruker(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: KontoInformasjonInput
    ): KontoDto {
        // TODO Validere kontonummer-input
        val kontonummer = soknadService.getKontonummer(soknadId)
        input.validate(kontonummer)

        val brukerdata = brukerdataService.updateKontoinformasjon(
            soknadId = soknadId,
            KontoInformasjonBruker(
                kontonummer = input.kontonummerBruker,
                harIkkeKonto = input.harIkkeKonto
            )
        )

        return KontoDto(
            harIkkeKonto = brukerdata.kontoInformasjon?.harIkkeKonto,
            kontonummerRegister = kontonummer,
            kontonummerBruker = brukerdata.kontoInformasjon?.kontonummer
        )
    }

    // TODO Skal bruker kunne velge at vedkommende ikke har kontonummer, selvom vi har ett?
    private fun KontoInformasjonInput.validate(kontonummerRegister: String?) {
        if (harIkkeKonto == true && (kontonummerBruker != null || kontonummerRegister != null)) {
            throw IllegalStateException("Bruker har kontonummer, men har krysset av for harIkkeKonto")
        }
    }
}

data class KontoDto(
    val harIkkeKonto: Boolean?,
    val kontonummerRegister: String?,
    val kontonummerBruker: String?
)

data class KontoInformasjonInput(
    val harIkkeKonto: Boolean?,
    val kontonummerBruker: String?
)
