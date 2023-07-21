package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.vedlegg.dto.FilFrontend

data class VedleggFrontend(
    val type: VedleggType,
    val gruppe: VedleggGruppe,
    val rader: List<VedleggRadFrontend>? = null,
    @Schema(description = "Ignoreres dersom alleredeLevert !== null", readOnly = true, deprecated = true)
    val vedleggStatus: VedleggStatus? = null,
    @Schema(description = "Vedlegg er levert inn utenom denne søknaden (f. eks. levert til NAV-kontor)")
    val alleredeLevert: Boolean? = null,
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

/**
 * Status for vedlegg til den aktuelle opplysningen.
 *
 * For front-end-bruk blir denne faset ut til fordel for [VedleggFrontend.alleredeLevert],
 * ettersom den egentlig indikerer to ortogonale tilstander:
 *
 * - vedlegg er lastet opp (som bør settes på backend)
 * - vedlegg er levert inn på annen måte (som bør settes på frontend)
 *
 * Ettersom "vedlegg er lastet opp" er mulig å utlede direkte fra [VedleggFrontend.filer],
 * kan dette forenkles til én boolean; [VedleggFrontend.alleredeLevert].
 */
enum class VedleggStatus {
    /** Minst en fil er lastet opp til vedlegg */
    LastetOpp,

    /** Ingen vedlegg er lastet opp */
    VedleggKreves,

    /** Bruker indikerer dokumentasjon er sendt inn på annen måte */
    VedleggAlleredeSendt;
}
