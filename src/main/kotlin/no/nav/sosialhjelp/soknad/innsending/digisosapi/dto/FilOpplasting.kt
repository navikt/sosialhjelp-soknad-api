package no.nav.sosialhjelp.soknad.innsending.digisosapi.dto

import java.io.InputStream

data class FilOpplasting(
    val metadata: FilMetadata,
    val data: InputStream
)

data class FilMetadata(
    val filnavn: String,
    val mimetype: String,
    val storrelse: Long
)

data class FilForOpplasting<T>(
    val filnavn: String,
    val metadata: T,
    val data: InputStream
)
