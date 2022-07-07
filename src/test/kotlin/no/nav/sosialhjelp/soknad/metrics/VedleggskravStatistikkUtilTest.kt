package no.nav.sosialhjelp.soknad.metrics

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus.LastetOpp
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus.VedleggAlleredeSendt
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus.VedleggKreves
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.JsonVedleggUtils
import no.nav.sosialhjelp.soknad.metrics.VedleggskravStatistikkUtil.genererVedleggskravStatistikk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VedleggskravStatistikkUtilTest {

    private val soknadUnderArbeidMock: SoknadUnderArbeid = mockk()

    @BeforeEach
    internal fun setUp() {
        every { soknadUnderArbeidMock.erEttersendelse } returns false
    }

    @Test
    fun reportVedleggskrav_shouldReportCorrect() {
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
        vedleggList.add(createVedleggMetadata(VedleggKreves, JsonVedleggUtils.ANNET, JsonVedleggUtils.ANNET))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 9, 3, 2, 4)
    }

    @Test
    fun reportVedleggskrav_with3LastetOpp_shouldReport3() {
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(LastetOpp, "skjema", "tillegg"))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 3, 3, 0, 0)
    }

    @Test
    fun reportVedleggskrav_with3Kreves_shouldReport3() {
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggKreves, "skjema", "tillegg"))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 3, 0, 0, 3)
    }

    @Test
    fun reportVedleggskrav_with3LevertTidligere_shouldReport3() {
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, "skjema", "tillegg"))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 3, 0, 3, 0)
    }

    @Test
    fun reportVedleggskrav_withAnnetLastetOpp_shouldReportZero() {
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(LastetOpp, JsonVedleggUtils.ANNET, JsonVedleggUtils.ANNET))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 0, 0, 0, 0)
    }

    @Test
    fun reportVedleggskrav_withAnnetKreves_shouldReportZero() {
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(VedleggKreves, JsonVedleggUtils.ANNET, JsonVedleggUtils.ANNET))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 0, 0, 0, 0)
    }

    @Test
    fun reportVedleggskrav_withAnnetLevertTidligere_shouldReportZero() {
        val vedleggList: MutableList<VedleggMetadata> = ArrayList()
        vedleggList.add(createVedleggMetadata(VedleggAlleredeSendt, JsonVedleggUtils.ANNET, JsonVedleggUtils.ANNET))

        val vedleggStatistikk = genererVedleggskravStatistikk(soknadUnderArbeidMock, vedleggList)
        verifyVedleggskravStatistikk(vedleggStatistikk, 0, 0, 0, 0)
    }

    private fun createVedleggMetadata(status: Vedleggstatus, skjema: String, tillegg: String): VedleggMetadata {
        val vedleggMetadata = VedleggMetadata()
        vedleggMetadata.status = status
        vedleggMetadata.skjema = skjema
        vedleggMetadata.tillegg = tillegg
        return vedleggMetadata
    }

    private fun verifyVedleggskravStatistikk(
        vedleggskravStatistikk: VedleggskravStatistikkUtil.VedleggskravStatistikk,
        expectedTotaltAntall: Int,
        expectedAntallInnsendt: Int,
        expectedAntallLevertTidligere: Int,
        expectedAntallIkkeLevert: Int,
    ) {
        assertThat(vedleggskravStatistikk.sendetype).isEqualTo("soknad")
        assertThat(vedleggskravStatistikk.totaltAntall).isEqualTo(expectedTotaltAntall)
        assertThat(vedleggskravStatistikk.antallInnsendt).isEqualTo(expectedAntallInnsendt)
        assertThat(vedleggskravStatistikk.antallLevertTidligere).isEqualTo(expectedAntallLevertTidligere)
        assertThat(vedleggskravStatistikk.antallIkkeLevert).isEqualTo(expectedAntallIkkeLevert)
    }
}