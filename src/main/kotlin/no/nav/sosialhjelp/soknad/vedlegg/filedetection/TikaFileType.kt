package no.nav.sosialhjelp.soknad.vedlegg.filedetection

enum class TikaFileType(val extension: String) {
    JPEG(".jpg"), PNG(".png"), PDF(".pdf"), UNKNOWN("")
}
