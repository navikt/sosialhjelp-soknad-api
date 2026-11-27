package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import java.util.UUID

class DokumentasjonIntegrationTest : AbstractIntegrationTest() {
    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = opprettSoknad(id = soknadId).also { soknadRepository.save(it) }
    }

    @Test
    fun `Skal returnere eksisterende Dokument`() {
        val filId = UUID.randomUUID()

        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            MellomlagringDto(
                navEksternRefId = soknad.id.toString(),
                mellomlagringMetadataList =
                    listOf(
                        MellomlagringDokumentInfo(
                            filnavn = pdfFil.name,
                            filId = filId.toString(),
                            storrelse = FileUtils.sizeOf(pdfFil),
                            mimetype = FileDetectionUtils.detectMimeType(pdfFil.readBytes()),
                        ),
                    ),
            )
        every { mellomlagringClient.hentDokument(any(), any()) } returns pdfFil.readBytes()

        doGetFullResponse(uri = getUrl(soknad.id, filId))
            .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*filename=\"${pdfFil.name}\".*")
            .expectBody(ByteArray::class.java)
            .returnResult().responseBody
            .also { bytes -> assertThat(bytes).isEqualTo(pdfFil.readBytes()) }
    }

    @Test
    fun `Skal returnere 404 nar dokument ikke finnes i mellomlagring`() {
        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            MellomlagringDto(soknad.id.toString(), emptyList())

        doGetFullResponse(uri = getUrl(soknad.id, UUID.randomUUID()))
            .expectStatus().isNotFound
    }

    private val pdfFil = ExampleFileRepository.PDF_FILE

    companion object {
        private fun getUrl(
            soknadId: UUID,
            dokumentId: UUID,
        ) = "/dokument/$soknadId/$dokumentId"
    }
}
