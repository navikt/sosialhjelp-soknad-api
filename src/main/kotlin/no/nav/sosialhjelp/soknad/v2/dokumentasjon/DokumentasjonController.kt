package no.nav.sosialhjelp.soknad.v2.dokumentasjon

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/dokumentasjon")
class DokumentasjonController(
    private val handler: DokumentasjonStatusUseCaseHandler,
) {
    @GetMapping("/forventet")
    fun getForventetDokumentasjon(
        @PathVariable("soknadId") soknadId: UUID,
    ): ForventetDokumentasjonDto {
        return handler.findForventetDokumentasjon(soknadId)
            .map { it.toDokumentasjonDto() }
            .let { ForventetDokumentasjonDto(it) }
    }

    @PutMapping
    fun updateDokumentasjonStatus(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: DokumentasjonInput,
    ): ForventetDokumentasjonDto {
        handler.updateDokumentasjonStatus(soknadId = soknadId, type = input.type, hasLevert = input.hasLevert)

        return handler.findForventetDokumentasjon(soknadId)
            .map { it.toDokumentasjonDto() }
            .let { ForventetDokumentasjonDto(it) }
    }
}

data class ForventetDokumentasjonDto(
    val dokumentasjon: List<DokumentasjonDto>,
)

data class DokumentasjonDto(
    val type: OpplysningType,
    val dokumentasjonStatus: DokumentasjonStatus,
    val dokumenter: List<DokumentDto>,
)

data class DokumentDto(
    val dokumentId: UUID,
    val filnavn: String,
)

private fun Dokumentasjon.toDokumentasjonDto(): DokumentasjonDto {
    return DokumentasjonDto(
        type = type,
        dokumentasjonStatus = status,
        dokumenter =
            dokumenter.map { dokument ->
                DokumentDto(dokumentId = dokument.dokumentId, filnavn = dokument.filnavn)
            },
    )
}

data class DokumentasjonInput(
    val type: OpplysningType,
    val hasLevert: Boolean,
)
