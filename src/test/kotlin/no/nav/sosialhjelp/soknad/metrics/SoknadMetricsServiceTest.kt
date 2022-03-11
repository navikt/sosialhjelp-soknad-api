package no.nav.sosialhjelp.soknad.metrics

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus.LastetOpp
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus.VedleggAlleredeSendt
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus.VedleggKreves
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import org.junit.jupiter.api.Test

internal class SoknadMetricsServiceTest {

    private val metricsService: SoknadMetricsService = mockk()

    @Test
    fun reportVedleggskrav_shouldReportCorrect() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(
            createVedleggMetadata(
                VedleggKreves,
                JsonVedleggUtils.ANNET,
                JsonVedleggUtils.ANNET
            )
        )
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 9, 3, 2, 4) }
    }

    @Test
    fun reportVedleggskrav_with3LastetOpp_shouldReport3() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 3, 3, 0, 0) }
    }

    @Test
    fun reportVedleggskrav_with3Kreves_shouldReport3() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 3, 0, 0, 3) }
    }

    @Test
    fun reportVedleggskrav_with3LevertTidligere_shouldReport3() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 3, 0, 3, 0) }
    }

    @Test
    fun reportVedleggskrav_withAnnetLastetOpp_shouldReportZero() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(LastetOpp, JsonVedleggUtils.ANNET, JsonVedleggUtils.ANNET))
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 0, 0, 0, 0) }
    }

    @Test
    fun reportVedleggskrav_withAnnetKreves_shouldReportZero() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(
            createVedleggMetadata(
                VedleggKreves,
                JsonVedleggUtils.ANNET,
                JsonVedleggUtils.ANNET
            )
        )
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 0, 0, 0, 0) }
    }

    @Test
    fun reportVedleggskrav_withAnnetLevertTidligere_shouldReportZero() {
        every { metricsService.countAndreportVedleggskrav(any(), any()) } answers { callOriginal() }
        every { metricsService.reportVedleggskrav(any(), any(), any(), any(), any()) } just runs
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(
            createVedleggMetadata(
                VedleggAlleredeSendt,
                JsonVedleggUtils.ANNET,
                JsonVedleggUtils.ANNET
            )
        )
        metricsService.countAndreportVedleggskrav(true, vedleggList)
        verify { metricsService.reportVedleggskrav(true, 0, 0, 0, 0) }
    }

    private fun createVedleggMetadata(status: Vedleggstatus, skjema: String, tillegg: String): VedleggMetadata {
        val vedleggMetadata = VedleggMetadata()
        vedleggMetadata.status = status
        vedleggMetadata.skjema = skjema
        vedleggMetadata.tillegg = tillegg
        return vedleggMetadata
    }
}
