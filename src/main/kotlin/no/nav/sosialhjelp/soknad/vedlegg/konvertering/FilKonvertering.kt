package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType

object FilKonvertering {
    fun konverterHvisStottet(sourceData: ByteArray, orginaltFilnavn: String): Pair<String, ByteArray> {
        val stottetFiltypeHvisFinnes = StottetFiltype.finnFiltype(detectMimeType(sourceData), orginaltFilnavn)

        return stottetFiltypeHvisFinnes?.let {
            val konvertertData = it.getFiltypeConverter().konverterTilPdf(sourceData)
            Pair(byttExtension(orginaltFilnavn), konvertertData)
        }
            ?: Pair(orginaltFilnavn, sourceData)
    }

    private fun byttExtension(filnavn: String): String {
        val indexOfExt = filnavn.lastIndexOf(".")

        return if (indexOfExt < 0) { "$filnavn.pdf" } else {
            val extension = filnavn.substring(indexOfExt)
            filnavn.replace(extension, ".pdf")
        }
    }
}
