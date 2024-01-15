package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import org.springframework.http.HttpStatusCode

interface FileConverter {
    fun toPdf(filename: String, bytes: ByteArray): ByteArray
}

data class FileConverterException(
    val httpStatus: HttpStatusCode,
    val msg: String,
    val trace: String,
) : SosialhjelpSoknadApiException("[$trace] Feil i filkonvertering: $httpStatus - $msg")
