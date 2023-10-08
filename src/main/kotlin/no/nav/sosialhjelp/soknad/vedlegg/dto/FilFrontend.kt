package no.nav.sosialhjelp.soknad.vedlegg.dto

data class FilFrontend(
    val filNavn: String?,
    val uuid: String? = null
)

data class FilInfoDTO (
    val filnavn: String,
    val uuid: String? = null
)

data class FilForVedlegg(
    val behandlingsId: String,
    val type: String,
    val filnavn: String,
    val data: ByteArray,
)