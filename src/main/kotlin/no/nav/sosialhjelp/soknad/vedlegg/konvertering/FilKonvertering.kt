package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.exception.FilKonverteringException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.KonverteringTilPdfException

object FilKonvertering {

    fun konverterHvisStottet(orginaltFilnavn: String, sourceData: ByteArray): Pair<String, ByteArray> {
        val konvertererHvisStottet = StottetFiltype.finnKonverterer(sourceData, orginaltFilnavn)

        return konvertererHvisStottet?.let {
            Pair(
                byttExtension(orginaltFilnavn),
                konverter(it.getFiltypeConverter(), sourceData)
            )
        }
            ?: Pair(orginaltFilnavn, sourceData)
    }

    private fun konverter(konverterer: FilTilPdfConverter, sourceData: ByteArray) =
        try {
            konverterer.konverterTilPdf(sourceData)
        } catch (e: FilKonverteringException) {
            throw KonverteringTilPdfException(e.message, e)
        }

    private fun byttExtension(filnavn: String): String {
        val indexOfExt = filnavn.lastIndexOf(".")

        return if (indexOfExt < 0) { "$filnavn.pdf" } else {
            val extension = filnavn.substring(indexOfExt)
            filnavn.replace(extension, ".pdf")
        }
    }
}
