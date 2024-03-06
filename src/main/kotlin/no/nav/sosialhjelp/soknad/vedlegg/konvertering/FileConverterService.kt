package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import org.springframework.stereotype.Service

@Service
class FileConverterService(
    val fileConverter: FileConverter
) {
    fun convertFileToPdf(name: String, bytes: ByteArray): ByteArray {
        require(bytes.isNotEmpty()) { "Fil [$name] for konvertering er tom." }

        return fileConverter.toPdf(name, bytes)
    }
}
