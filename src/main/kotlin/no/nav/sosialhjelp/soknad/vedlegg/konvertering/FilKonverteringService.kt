package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import org.springframework.web.multipart.MultipartFile

interface FilKonverteringService {
    fun konverterFilTilPdf(fil: MultipartFile): Pair<String, ByteArray>
}
