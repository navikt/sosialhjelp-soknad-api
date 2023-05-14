package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.mapToTikaType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.*

internal class MellomlagringServiceTest {

    private val mellomlagringClient: MellomlagringClient = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val virusScanner: VirusScanner = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()

    private val mellomlagringService = spyk(
        MellomlagringService(mellomlagringClient, soknadUnderArbeidRepository, virusScanner, soknadUnderArbeidService)
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.environmentName } returns "test"
        every { MiljoUtils.isNonProduction() } returns true

        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { virusScanner.scan(any(), any(), any(), any()) } just runs
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    internal fun getAllVedlegg() {
        // client returnerer null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns null
        assertThat(mellomlagringService.getAllVedlegg("behandlingsId")).isEmpty()

        // mellomlagringMetadataList er null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = null
        )
        assertThat(mellomlagringService.getAllVedlegg("behandlingsId")).isEmpty()

        // mellomlagringMetadataList har innhold
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        val allVedlegg = mellomlagringService.getAllVedlegg("behandlingsId")
        assertThat(allVedlegg).hasSize(1)
        assertThat(allVedlegg[0].filnavn).isEqualTo("filnavn")
    }

    @Test
    internal fun getVedlegg() {
        // client returnerer null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns null
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }

        // mellomlagringMetadataList er null
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = null
        )
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }

        // mellomlagringMetadataList har innhold, men finner ikke samme vedleggId
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }

        // mellomlagringMetadataList har innhold og vedleggId finnes
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "vedleggId", storrelse = 123L, mimetype = "mime")
            )
        )
        every { mellomlagringClient.getVedlegg(any(), any()) } returns "hello-world".encodeToByteArray()
        val mellomlagretVedlegg = mellomlagringService.getVedlegg("behandlingsId", "vedleggId")
        assertThat(mellomlagretVedlegg?.data).hasSize("hello-world".length)
        assertThat(mellomlagretVedlegg?.filnavn).isEqualTo("filnavn")
    }

    @Test
    fun `Test upload pdf-vedlegg`() {
        val bytes = PDF_FILE.readBytes()

        val behandlingsId = "123"

        val mellomlagringDokumentInfos = opprettDokumentInfoList(PDF_FILE)
        val eksternId = createPrefixedBehandlingsId(behandlingsId)

        every { mellomlagringClient.postVedlegg(eksternId, any()) } just runs
        every { mellomlagringClient.getMellomlagredeVedlegg(eksternId) } returns MellomlagringDto(eksternId, mellomlagringDokumentInfos)

        every {
            mellomlagringService.uploadVedlegg(
                behandlingsId, "hei|på deg", bytes, PDF_FILE.name
            )
        } answers { callOriginal() }

        val (sha512, vedleggMetadata) = mellomlagringService
            .uploadVedlegg("123", "hei|på deg", bytes, "sample_pdf.pdf")

        assertThat(sha512).isEqualTo(getSha512FromByteArray(bytes))

        val filnavn = lagFilnavn(
            PDF_FILE.name,
            mapToTikaType(detectMimeType(bytes)),
            UUID.nameUUIDFromBytes(bytes).toString()
        )
        assertThat(vedleggMetadata.filnavn).isEqualTo(filnavn)
    }

    private fun opprettDokumentInfoList(fil: File): List<MellomlagringDokumentInfo> {
        val bytes = fil.readBytes()
        return listOf(
            MellomlagringDokumentInfo(
                lagFilnavn(fil.name, validerFil(bytes, fil.name), UUID.nameUUIDFromBytes(bytes).toString()),
                UUID.nameUUIDFromBytes(bytes).toString(),
                Files.size(fil.toPath()),
                detectMimeType(bytes)
            )
        )
    }
}
