package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.exceptions.Feilmelding
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettDokumentasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.toSha512
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import java.util.UUID

class DokumentasjonIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = opprettSoknad().also { soknadRepository.save(it) }
    }

    @Test
    fun `Skal returnere eksisterende Dokument`() {
        every { mellomlagringClient.getDokument(any(), any()) } returns pdfFil.readBytes()

        val dokumentId = saveDokumentasjonAndReturnDokumentId()

        doGetFullResponse(uri = getUrl(soknad.id, dokumentId))
            .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*filename=\"${pdfFil.name}\".*")
            .expectBody(ByteArray::class.java)
            .returnResult().responseBody
            .also { bytes -> assertThat(bytes).isEqualTo(pdfFil.readBytes()) }
    }

    @Test
    fun `Dokument som ikke finnes skal gi feil og at den ble slettes hos mellomlagrind`() {
        every { mellomlagringClient.deleteDokument(any(), any()) } just runs

        doGetFullResponse(uri = getUrl(soknad.id, UUID.randomUUID()))
            .expectStatus().isNotFound
            .expectBody(Feilmelding::class.java)
            .returnResult().responseBody!!
            .also {
                assertThat(it.message).isEqualTo("Dokument eksisterer ikke på noe Dokumentasjon")
            }

        verify(exactly = 1) { mellomlagringClient.deleteDokument(any(), any()) }
    }

    @Test
    fun `Dokument som ikke finnes i Mellomlagring skal slettes lokalt`() {
        every { mellomlagringClient.getDokument(any(), any()) } throws IkkeFunnetException("Dokument ikke funnet hos Fiks")

        val dokumentId = saveDokumentasjonAndReturnDokumentId()

        val response = doGetFullResponse(getUrl(soknad.id, dokumentId))
        response
            .expectStatus().isNotFound

        assertThat(dokumentasjonRepository.findDokumentBySoknadId(soknad.id, dokumentId)).isNull()
    }

    @Test
    fun `Laste opp dokument skal lagres i db og oppdatere dokumentasjonsstatus`() {
        val filnavnSlot = slot<String>()
        every { mellomlagringClient.postDokument(any(), capture(filnavnSlot), any()) } just runs
        every { mellomlagringClient.getDokumentMetadata(any()) } answers { createMellomlagringDto(filnavnSlot.captured) }

        Dokumentasjon(
            soknadId = soknad.id,
            type = InntektType.UTBETALING_UTBYTTE,
            status = DokumentasjonStatus.FORVENTET,
        )
            .also { dokumentasjonRepository.save(it) }

        val dokumentDto =
            doPost(
                uri = saveUrl(soknad.id, InntektType.UTBETALING_UTBYTTE),
                requestBody = createFileUpload(),
                responseBodyClass = DokumentDto::class.java,
                soknadId = soknad.id,
            )
                .also { dto -> assertThat(dto.filnavn).isEqualTo(filnavnSlot.captured) }

        dokumentasjonRepository.findAllBySoknadId(soknad.id)
            .let { dokList ->
                assertThat(dokList).hasSize(1)
                dokList.first()
            }
            .let { dokumentasjon ->
                assertThat(dokumentasjon.status).isEqualTo(DokumentasjonStatus.LASTET_OPP)
                assertThat(dokumentasjon.dokumenter).hasSize(1)
                dokumentasjon.dokumenter.first()
            }
            .also { dokument ->
                assertThat(dokument.dokumentId).isEqualTo(dokumentDto.dokumentId)
                assertThat(dokument.filnavn).isEqualTo(dokumentDto.filnavn)
                assertThat(dokument.sha512).isEqualTo(VedleggUtils.getSha512FromByteArray(pdfFil.readBytes()))
            }
    }

    @Test
    fun `Last opp Dokument til ikke-eksisterende Dokumentasjon skal gi feil`() {
        doPostFullResponse(
            uri = saveUrl(soknad.id, UtgiftType.UTGIFTER_BOLIGLAN),
            requestBody = createFileUpload(),
            soknadId = soknad.id,
        )
            .expectStatus().isNotFound
            .expectBody(Feilmelding::class.java)
            .returnResult().responseBody!!
            .also {
                assertThat(it.message).isEqualTo("Dokumentasjon for type UTGIFTER_BOLIGLAN finnes ikke")
            }
    }

    @Test
    fun `Slette siste Dokument i Dokumentasjon skal endre status`() {
        every { mellomlagringClient.deleteDokument(any(), any()) } just runs

        val dokumentasjon = opprettDokumentasjon(soknadId = soknad.id).also { dokumentasjonRepository.save(it) }
        assertThat(dokumentasjon.status).isEqualTo(DokumentasjonStatus.LASTET_OPP)
        assertThat(dokumentasjon.dokumenter).hasSize(1)

        dokumentasjon.dokumenter.first().also {
            doDelete(
                uri = deleteUrl(soknad.id, it.dokumentId),
                soknadId = soknad.id,
            )
        }

        dokumentasjonRepository.findBySoknadIdAndType(soknad.id, dokumentasjon.type)!!
            .also {
                assertThat(it.dokumenter).isEmpty()
                assertThat(it.status).isEqualTo(DokumentasjonStatus.FORVENTET)
            }
    }

    private fun createFileUpload(): LinkedMultiValueMap<String, Any> {
        return LinkedMultiValueMap<String, Any>()
            .apply {
                val resource =
                    object : ByteArrayResource(pdfFil.readBytes()) {
                        override fun getFilename(): String? {
                            return pdfFil.name
                        }
                    }
                add("file", resource)
            }
    }

    private fun createMellomlagringDto(filnavn: String): MellomlagringDto {
        return MellomlagringDto(
            navEksternRefId = soknad.id.toString(),
            mellomlagringMetadataList =
                listOf(
                    MellomlagringDokumentInfo(
                        filnavn = filnavn,
                        filId = UUID.randomUUID().toString(),
                        storrelse = FileUtils.sizeOf(pdfFil),
                        mimetype = FileDetectionUtils.detectMimeType(pdfFil.readBytes()),
                    ),
                ),
        )
    }

    private fun saveDokumentasjonAndReturnDokumentId(): UUID {
        return opprettDokumentasjon(
            soknadId = soknad.id,
            status = DokumentasjonStatus.LASTET_OPP,
            dokumenter = setOf(Dokument(UUID.randomUUID(), pdfFil.name, pdfFil.readBytes().toSha512())),
        )
            .also { dokumentasjonRepository.save(it) }.dokumenter.first().dokumentId
    }

    companion object {
        private fun getUrl(
            soknadId: UUID,
            dokumentId: UUID,
        ) = "/dokument/$soknadId/$dokumentId"

        private fun saveUrl(
            soknadId: UUID,
            type: OkonomiType,
        ) = "/dokument/$soknadId/$type"

        private fun deleteUrl(
            soknadId: UUID,
            dokumentId: UUID,
        ) = "/dokument/$soknadId/$dokumentId"

        private val pdfFil = ExampleFileRepository.PDF_FILE
    }
}

private fun DokumentasjonRepository.findDokumentBySoknadId(
    soknadId: UUID,
    dokumentId: UUID,
): Dokument? {
    return findAllBySoknadId(soknadId)
        .flatMap { it.dokumenter }
        .find { it.dokumentId == dokumentId }
}
