package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend

data class VedleggFrontend(
    val type: VedleggType,
    val gruppe: VedleggGruppe,
    val rader: List<VedleggRadFrontend> = emptyList(),
    @Schema(readOnly = true)
    val vedleggStatus: VedleggStatus = VedleggStatus.Ukjent,
    @Schema(readOnly = true)
    val filer: List<FilFrontend> = emptyList(),
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VedleggRadFrontend(
    @Schema(nullable = true)
    val beskrivelse: String? = null,
    @Schema(nullable = true)
    val belop: Int? = null,
    @Schema(nullable = true)
    val brutto: Int? = null,
    @Schema(nullable = true)
    val netto: Int? = null,
    @Schema(nullable = true)
    val avdrag: Int? = null,
    @Schema(nullable = true)
    var renter: Int? = null
)

enum class VedleggStatus {
    Ukjent, LastetOpp, VedleggKreves, VedleggAlleredeSendt;
}
