package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import org.springframework.http.HttpStatusCode

interface FileConverter {
    fun toPdf(
        filename: String,
        bytes: ByteArray,
    ): ByteArray
}

data class FileConversionException(
    val httpStatus: HttpStatusCode,
    val msg: String,
) : SosialhjelpSoknadApiException("Feil i filkonvertering: $httpStatus - $msg")
