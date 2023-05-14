package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType

object FilKonvertering {
    fun konverterHvisStottet(sourceData: ByteArray, filnavn: String): VedleggWrapper {
        val stottetFiltypeHvisFinnes = StottetFiltype.finnFiltype(detectMimeType(sourceData), filnavn)

        return stottetFiltypeHvisFinnes?.let {
            val konvertertData = it.getFiltypeConverter().konverterTilPdf(sourceData)
            VedleggWrapper(konvertertData, byttExtension(filnavn))
        }
            ?: VedleggWrapper(sourceData, filnavn)
    }

    private fun byttExtension(filnavn: String): String = with(filnavn) {
        val oldExtension = substring(lastIndexOf("."))
        replace(oldExtension, ".pdf")
    }
}

data class VedleggWrapper(
    val data: ByteArray,
    val filnavn: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VedleggWrapper

        if (!data.contentEquals(other.data)) return false
        return filnavn == other.filnavn
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + filnavn.hashCode()
        return result
    }
}
