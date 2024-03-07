package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.sosialhjelp.soknad.vedlegg.FileConversionUpload
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FileConverterService(
    val fileConverter: FileConverter,
    private val meterRegistry: MeterRegistry
) {
    private val pdfConversionSuccess = Counter.builder("soknad_pdf_conversion_success")
    private val pdfConversionFailure = Counter.builder("soknad_pdf_conversion_failure")

    fun convertFileToPdf(file: FileConversionUpload): ByteArray {
        log.info("Konverterer upload {} til PDF", file)

        return runCatching {
            fileConverter.toPdf(file.unconvertedName, file.bytes)
        }
            .onSuccess { pdfBytes ->
                check(pdfBytes.isNotEmpty()) { "Konvertert fil [$file] er tom." }
                pdfConversionSuccess
                    .tag(TAG_FILE_TYPE, "${file.mimeType}/${file.extension}")
                    .register(meterRegistry)
                    .increment()
            }.onFailure { e ->
                log.error("Feil ved konvertering av fil [$file]", e)
                pdfConversionFailure
                    .tag(TAG_FILE_TYPE, "${file.mimeType}/${file.extension}")
                    .tag(TAG_ERROR_CLASS, "${e::class}")
                    .register(meterRegistry)
                    .increment()
            }.getOrThrow()
    }

    companion object {
        private const val TAG_FILE_TYPE = "mime_type"
        private const val TAG_ERROR_CLASS = "error_class"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
