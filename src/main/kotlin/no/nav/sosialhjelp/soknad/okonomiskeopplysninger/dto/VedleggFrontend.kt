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
    val filer: List<FilFrontend>?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VedleggRadFrontend(
    val beskrivelse: String? = null,
    val belop: Int? = null,
    val brutto: Int? = null,
    val netto: Int? = null,
    val avdrag: Int? = null,
    var renter: Int? = null
)

enum class VedleggStatus {
    Ukjent, LastetOpp, VedleggKreves, VedleggAlleredeSendt;
}
