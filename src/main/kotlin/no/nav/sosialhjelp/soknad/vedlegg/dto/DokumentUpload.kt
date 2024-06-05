package no.nav.sosialhjelp.soknad.vedlegg.dto

import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata

data class DokumentUpload(
    val filename: String,
    val dokumentId: String,
) {
    // For kompatibilitet med gammel frontend kopierer vi verdiene til gamle feltnavn.
    val uuid: String
        get() = dokumentId

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
