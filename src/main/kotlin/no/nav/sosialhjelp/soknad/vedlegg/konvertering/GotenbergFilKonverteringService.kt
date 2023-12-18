package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class GotenbergFilKonverteringService(
    val konvertereTilPdfClient: KonvertereTilPdfClient
): FilKonverteringService {

    override fun konverterFilTilPdf(fil: MultipartFile): Pair<String, ByteArray> {
        val pdfBytes = konvertereTilPdfClient.konvertereTilPdf(fil)



        val extensionIndex = originaltFilnavn.lastIndexOf(".")
        val nyttFilnavn = "${originaltFilnavn.substring(0, extensionIndex)}.pdf"

    }

}
