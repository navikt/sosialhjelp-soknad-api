package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.FilTilPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.csv.CsvToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.excel.ExcelToPdfConverter
import no.nav.sosialhjelp.kotlin.utils.pdf.filkonvertering.word.WordToPdfConverter

interface FiltypeConverter {
    fun getFiltypeConverter(): FilTilPdfConverter
}
enum class StottetFiltype(val mimeType: String, val extension: String) : FiltypeConverter {

    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx") {
        override fun getFiltypeConverter() = ExcelToPdfConverter
    },
    WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx") {
        override fun getFiltypeConverter() = WordToPdfConverter
    },
    CSV("text/plain", ".csv") {
        override fun getFiltypeConverter() = CsvToPdfConverter
    };

    companion object FiltypeUtil {
        fun finnFiltype(mimeType: String, filnavn: String) =
            StottetFiltype.values().find { type -> mimeType == type.mimeType && filnavn.contains(type.extension) }
    }
}
