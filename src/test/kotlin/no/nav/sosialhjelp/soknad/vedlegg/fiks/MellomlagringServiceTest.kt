package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MellomlagringServiceTest {

    private val mellomlagringClient: MellomlagringClient = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val virusScanner: VirusScanner = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()

    private val mellomlagringService = MellomlagringService(
        mellomlagringClient,
        soknadUnderArbeidRepository,
        virusScanner,
        soknadUnderArbeidService
    )

    @BeforeEach
    internal fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.environmentName } returns "test"
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
}
