package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
    ): KontoInformasjonDto = eierService.findOrError(soknadId).kontonummer.toKontoInformasjonDto()

    @PutMapping
    fun updateKontoInformasjonBruker(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: KontoInput,
    ): KontoInformasjonDto =
        eierService
            .run {
                when (input) {
                    is HarIkkeKontoInput -> updateKontonummer(soknadId, harIkkeKonto = true)
                    is KontonummerBrukerInput -> updateKontonummer(soknadId, kontonummerBruker = input.kontonummerBruker)
                }
            }.toKontoInformasjonDto()
}

private fun Kontonummer.toKontoInformasjonDto(): KontoInformasjonDto =
    KontoInformasjonDto(
        harIkkeKonto = harIkkeKonto,
        kontonummerRegister = fraRegister,
        kontonummerBruker = fraBruker,
    )

data class KontoInformasjonDto(
    val harIkkeKonto: Boolean? = null,
    val kontonummerRegister: String? = null,
    val kontonummerBruker: String? = null,
)

// JsonIgnoreProperties kan fjernes når frontend er oppgradert
@JsonIgnoreProperties("type")
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = KontonummerBrukerInput::class)
@JsonSubTypes(JsonSubTypes.Type(HarIkkeKontoInput::class), JsonSubTypes.Type(KontonummerBrukerInput::class))
sealed interface KontoInput

data class HarIkkeKontoInput(val harIkkeKonto: Boolean) : KontoInput

// JsonAlias kan fjernes når frontend er oppgradert
data class KontonummerBrukerInput(
    @JsonAlias("kontonummer") val kontonummerBruker: String?,
) : KontoInput
