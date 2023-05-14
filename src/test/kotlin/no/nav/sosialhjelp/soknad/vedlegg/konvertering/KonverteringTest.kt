package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE_OLD
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.CSV
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.EXCEL
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.FiltypeUtil.finnFiltype
import no.nav.sosialhjelp.soknad.vedlegg.konvertering.StottetFiltype.WORD
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KonverteringTest {
    @Test
    fun `Test konverter excel-fil (xlsx) stottes`() {
        val fil = EXCEL_FILE
        val filtype = finnFiltype(detectMimeType(fil.readBytes()), fil.name)
        assertThat(filtype).isEqualTo(EXCEL)

        val konvertertFil = filtype!!.getFiltypeConverter().konverterTilPdf(fil.readBytes())
        val detectMimeType = detectMimeType(konvertertFil)

        assertThat(detectMimeType).isEqualTo("application/pdf")
    }

    @Test
    fun `Test excel-fil (xls) stottes ikke`() {
        val fil = EXCEL_FILE_OLD
        val filtype = finnFiltype(detectMimeType(fil.readBytes()), fil.name)
        assertThat(filtype).isNull()
    }

    @Test
    fun `Test word-fil (docx) stottes`() {
        val fil = WORD_FILE
        val filtype = finnFiltype(detectMimeType(fil.readBytes()), fil.name)
        assertThat(filtype).isEqualTo(WORD)
    }

    @Test
    fun `Test word-fil (doc) stottes ikke`() {
        val fil = WORD_FILE_OLD
        val filtype = finnFiltype(detectMimeType(fil.readBytes()), fil.name)
        assertThat(filtype).isNull()
    }

    @Test
    fun `Test csv-fil (csv) stottes`() {
        val fil = CSV_FILE
        val filtype = finnFiltype(detectMimeType(fil.readBytes()), fil.name)
        assertThat(filtype).isEqualTo(CSV)
    }
}
