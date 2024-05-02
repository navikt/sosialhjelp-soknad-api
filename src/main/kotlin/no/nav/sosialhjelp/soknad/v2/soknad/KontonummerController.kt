package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
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
    ): KontoInformasjonDto {
        return eierService.findOrError(soknadId).kontonummer.toKontoInformasjonDto()
    }

    @PutMapping
    fun updateKontoInformasjonBruker(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody(required = true) input: KontoInput,
    ): KontoInformasjonDto {
        return eierService.run {
            when (input) {
                is HarIkkeKontoInput -> updateKontonummer(soknadId = soknadId, harIkkeKonto = true)
                is KontonummerBrukerInput ->
                    updateKontonummer(soknadId = soknadId, kontonummerBruker = input.kontonummer)
                else -> throw IkkeFunnetException("Ukjent KontoInput-type")
            }
        }.toKontoInformasjonDto()
    }
}

private fun Kontonummer.toKontoInformasjonDto(): KontoInformasjonDto {
    return KontoInformasjonDto(
        harIkkeKonto = harIkkeKonto,
        kontonummerRegister = fraRegister,
        kontonummerBruker = fraBruker,
    )
}

data class KontoInformasjonDto(
    val harIkkeKonto: Boolean? = null,
    val kontonummerRegister: String? = null,
    val kontonummerBruker: String? = null,
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
)
@JsonSubTypes(
    JsonSubTypes.Type(HarIkkeKontoInput::class),
    JsonSubTypes.Type(KontonummerBrukerInput::class),
)
interface KontoInput

data class HarIkkeKontoInput(
    val harIkkeKonto: Boolean,
) : KontoInput

data class KontonummerBrukerInput(
    val kontonummer: String,
) : KontoInput
