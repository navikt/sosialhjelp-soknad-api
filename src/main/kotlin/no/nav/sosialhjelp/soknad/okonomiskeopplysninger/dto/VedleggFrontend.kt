package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend

data class VedleggFrontend(
    val type: VedleggType,
    val gruppe: VedleggGruppe,
    val rader: List<VedleggRadFrontend>? = null,
    @Schema(readOnly = true)
    val vedleggStatus: VedleggStatus? = null,
    @Schema(readOnly = true)
    val filer: List<FilFrontend>?,
)

data class VedleggRadFrontend(
    val beskrivelse: String? = null,
    val belop: Int? = null,
    val brutto: Int? = null,
    val netto: Int? = null,
    val avdrag: Int? = null,
    var renter: Int? = null
)

enum class VedleggStatus {
    LastetOpp, VedleggKreves, VedleggAlleredeSendt;
}
