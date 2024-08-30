package no.nav.sosialhjelp.soknad.vedlegg.dto

import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata

data class DokumentUpload(
    val filename: String,
    val dokumentId: String,
) {
    // For kompatibilitet med gammel frontend kopierer vi verdiene til gamle feltnavn.
    @get:Schema(hidden = true)
    val uuid: String
        get() = dokumentId

    @get:Schema(hidden = true)
    val filNavn: String
        get() = filename

    companion object {
        fun fromMellomlagretVedleggMetadata(mellomlagretVedleggMetadata: MellomlagretVedleggMetadata) =
            DokumentUpload(
                filename = mellomlagretVedleggMetadata.filnavn,
                dokumentId = mellomlagretVedleggMetadata.filId,
            )
    }
}
