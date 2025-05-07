package no.nav.sosialhjelp.soknad.v2.soknad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
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
                    is HarIkkeKontoInput -> updateKontonummer(soknadId = soknadId, harIkkeKonto = true)
                    is KontonummerBrukerInput ->
                        updateKontonummer(soknadId = soknadId, kontonummerBruker = input.kontonummer)
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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(HarIkkeKontoInput::class, name = "HarIkkeKonto"),
    JsonSubTypes.Type(KontonummerBrukerInput::class, name = "KontonummerBruker"),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "HarIkkeKonto", schema = HarIkkeKontoInput::class),
        DiscriminatorMapping(value = "KontonummerBruker", schema = KontonummerBrukerInput::class),
    ],
    subTypes = [
        HarIkkeKontoInput::class,
        KontonummerBrukerInput::class,
    ],
)
sealed interface KontoInput

data class HarIkkeKontoInput(
    val harIkkeKonto: Boolean,
) : KontoInput

data class KontonummerBrukerInput(
    val kontonummer: String?,
) : KontoInput
