package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import org.springframework.http.HttpStatusCode

interface FileConverter {
    fun toPdf(filename: String, bytes: ByteArray): ByteArray
}

data class FileConverterException(
    val httpStatus: HttpStatusCode,
    val msg: String,
    val trace: String
): RuntimeException("$trace Feil i filkonvertering: $httpStatus - $msg")