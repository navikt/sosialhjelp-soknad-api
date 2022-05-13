package no.nav.sosialhjelp.soknad.vedlegg.fiks

data class MellomlagringDto(
    val navEksternRefId: String,
    val mellomlagringMetadataList: List<MellomlagringDokumentInfo>?
)

data class MellomlagringDokumentInfo(
    val filnavn: String,
    val filId: String,
    val storrelse: Long,
    val mimetype: String
)
