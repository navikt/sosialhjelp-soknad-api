package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.springframework.stereotype.Service

@Service
class FileConverterService (
    val fileConverter: FileConverter,
    val virusScanner: VirusScanner
) {
    fun convertFileToPdf(name: String, bytes: ByteArray): Pair<String, ByteArray> {

        if (bytes.isEmpty()) throw IllegalStateException("Fil \"$name\" for konvertering er tom.")

        return Pair(
            replaceExtension(name),
            fileConverter.toPdf(name, bytes)
        )
    }

    private fun replaceExtension(name: String): String {
        val extensionIndex = name.lastIndexOf(".")
        return name.replace(name.substring(extensionIndex), ".pdf")
    }
}
