package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.eier.Kontonummer
import no.nav.sosialhjelp.soknad.v2.eier.service.EierService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/personalia/kontonummer")
class KontonummerController(
    private val eierService: EierService,
) {
    @GetMapping
    fun getKontonummer(
        @PathVariable("soknadId") soknadId: UUID,
    ): KontoinformasjonDto = eierService.findOrError(soknadId).kontonummer.toDTO()

    @PutMapping
    fun updateKontoInformasjonBruker(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: KontoinformasjonInput,
    ): KontoinformasjonDto = eierService.updateKontonummer(soknadId, input.kontonummerBruker, input.harIkkeKonto).toDTO()

    private fun Kontonummer.toDTO(): KontoinformasjonDto = KontoinformasjonDto(harIkkeKonto, fraRegister, fraBruker)
}

data class KontoinformasjonDto(
    val harIkkeKonto: Boolean? = null,
    val kontonummerRegister: String? = null,
    val kontonummerBruker: String? = null,
)

// JsonIgnoreProperties er her for bakoverkompat og kan fjernes når frontend er oppdatert
@JsonIgnoreProperties("type")
data class KontoinformasjonInput(
    val harIkkeKonto: Boolean? = null,
    // JsonAlias er her for bakoverkompat og kan fjernes når frontend er oppdatert
    @JsonAlias("kontonummer")
    val kontonummerBruker: String? = null,
)
