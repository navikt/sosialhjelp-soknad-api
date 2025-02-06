package no.nav.sosialhjelp.soknad.v2.integrationtest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadApiError
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DocumentValidator
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.v2.opprettDokumentasjon
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
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
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import java.util.UUID

class DokumentasjonIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var dokumentasjonRepository: DokumentasjonRepository

    @Autowired
    private lateinit var documentValidator: DocumentValidator

    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    private lateinit var soknad: Soknad

    @BeforeEach
    fun setup() {
        soknad = opprettSoknad().also { soknadRepository.save(it) }
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

        val dokumentId = saveDokumentasjonAndReturnDokumentId(filId)

        doGetFullResponse(uri = getUrl(soknad.id, dokumentId))
            .expectHeader().valueMatches(HttpHeaders.CONTENT_DISPOSITION, ".*filename=\"${pdfFil.name}\".*")
            .expectBody(ByteArray::class.java)
            .returnResult().responseBody
            .also { bytes -> assertThat(bytes).isEqualTo(pdfFil.readBytes()) }
    }

    @Test
    fun `Dokument som ikke finnes skal gi feil og slettes i mellomlagring`() {
        every { mellomlagringClient.slettDokument(any(), any()) } just runs

        doGetFullResponse(uri = getUrl(soknad.id, UUID.randomUUID()))
            .expectStatus().isNotFound
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody!!
            .also {
                assertThat(it.message).contains("Fant ikke dokumentreferanse")
            }

        verify(exactly = 1) { mellomlagringClient.slettDokument(any(), any()) }
    }

    @Test
    fun `Dokument som ikke finnes i Mellomlagring skal slettes lokalt`() {
        every { mellomlagringClient.slettDokument(any(), any()) } throws IkkeFunnetException("Dokument ikke funnet hos Fiks")

        val dokumentId = saveDokumentasjonAndReturnDokumentId()
        assertThat(dokumentasjonRepository.findDokumentBySoknadId(soknad.id, dokumentId)).isNotNull()

        val response = doGetFullResponse(getUrl(soknad.id, dokumentId))
        response
            .expectStatus().isNotFound

        assertThat(dokumentasjonRepository.findDokumentBySoknadId(soknad.id, dokumentId)).isNull()
    }

    @Test
    fun `Laste opp dokument skal lagres i db og oppdatere dokumentasjonsstatus`() {
        val filnavnSlot = slot<String>()

        every { mellomlagringClient.lastOppDokument(soknad.id.toString(), capture(filnavnSlot), any()) } answers {
            createMellomlagringDto(
                metadataList =
                    listOf(
                        MellomlagringDokumentInfo(
                            filnavn = filnavnSlot.captured,
                            filId = UUID.randomUUID().toString(),
                            storrelse = FileUtils.sizeOf(pdfFil),
                            mimetype =
                                FileDetectionUtils.detectMimeType(
                                    pdfFil.readBytes(),
                                ),
                        ),
                    ),
            )
        }

        Dokumentasjon(
            soknadId = soknad.id,
            type = InntektType.UTBETALING_UTBYTTE,
            status = DokumentasjonStatus.FORVENTET,
        )
            .also { dokumentasjonRepository.save(it) }

        val dokumentDto =
            doPostWithBody(
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
            }
    }

    @Test
    fun `Last opp Dokument til ikke-eksisterende Dokumentasjon skal gi feil`() {
        every { mellomlagringClient.lastOppDokument(any(), any(), any()) } answers {
            MellomlagringDto(
                navEksternRefId = soknad.id.toString(),
                mellomlagringMetadataList =
                    listOf(
                        MellomlagringDokumentInfo(
                            filnavn = pdfFil.name,
                            filId = UUID.randomUUID().toString(),
                            storrelse = FileUtils.sizeOf(pdfFil),
                            mimetype = FileDetectionUtils.detectMimeType(pdfFil.readBytes()),
                        ),
                    ),
            )
        }

        doPostFullResponse(
            uri = saveUrl(soknad.id, UtgiftType.UTGIFTER_BOLIGLAN),
            requestBody = createFileUpload(),
            soknadId = soknad.id,
            contentType = MediaType.MULTIPART_FORM_DATA,
        )
            .expectStatus().isNotFound
            .expectBody(SoknadApiError::class.java)
            .returnResult().responseBody!!
            .also {
                assertThat(it.message).isEqualTo("Dokumentasjon for type UTGIFTER_BOLIGLAN finnes ikke")
            }
    }

    @Test
    fun `Slette siste Dokument i Dokumentasjon skal endre status`() {
        every { mellomlagringClient.slettDokument(any(), any()) } just runs

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

    @Test
    fun `Validere dokumenter hvor alle finnes skal ikke endre noe`() {
        val metadataList =
            listOf(
                createMellomlagringDokumentInfo("1.pdf", filId = UUID.nameUUIDFromBytes("1".toByteArray()).toString()),
                createMellomlagringDokumentInfo("2.pdf", filId = UUID.nameUUIDFromBytes("2".toByteArray()).toString()),
                createMellomlagringDokumentInfo("3.pdf", filId = UUID.nameUUIDFromBytes("3".toByteArray()).toString()),
            )

        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            createMellomlagringDto(
                metadataList = metadataList,
            )

        opprettDokumentasjon(
            soknadId = soknad.id,
            dokumenter =
                metadataList.map {
                    DokumentRef(
                        dokumentId = UUID.fromString(it.filId),
                        filnavn = it.filnavn,
                        sha512 = getSha512FromByteArray(it.filnavn.toByteArray()),
                    )
                }.toSet(),
        ).also { dokumentasjonRepository.save(it) }

        documentValidator.validateDocumentsExistsInMellomlager(soknad.id)

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it).hasSize(1)
            assertThat(it.first().dokumenter).hasSize(3)
        }

        dokumentasjonRepository.deleteAll()
    }

    @Test
    fun `Validere dokumenter hvor det finnes ekstra dokumenter i mellomlager skal ikke gjore noe`() {
        val metadataList =
            listOf(
                createMellomlagringDokumentInfo("1.pdf", filId = UUID.nameUUIDFromBytes("1".toByteArray()).toString()),
                createMellomlagringDokumentInfo("2.pdf", filId = UUID.nameUUIDFromBytes("2".toByteArray()).toString()),
                createMellomlagringDokumentInfo("3.pdf", filId = UUID.nameUUIDFromBytes("3".toByteArray()).toString()),
            )

        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            createMellomlagringDto(
                metadataList = metadataList,
            )

        opprettDokumentasjon(
            soknadId = soknad.id,
            dokumenter =
                metadataList
                    .filter { it.filnavn != "3.pdf" }
                    .map {
                        DokumentRef(
                            UUID.fromString(it.filId),
                            it.filnavn,
                            getSha512FromByteArray(it.filnavn.toByteArray()),
                        )
                    }.toSet(),
        ).also { dokumentasjonRepository.save(it) }

        documentValidator.validateDocumentsExistsInMellomlager(soknad.id)

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it).hasSize(1)
            assertThat(it.first().dokumenter).hasSize(2)
        }

        dokumentasjonRepository.deleteAll()
    }

    @Test
    fun `Validere dokumenter hvor Dokument ikke eksisterer i mellomlager skal slette dokument`() {
        val metadataList =
            listOf(
                createMellomlagringDokumentInfo("1.pdf", filId = UUID.nameUUIDFromBytes("1".toByteArray()).toString()),
                createMellomlagringDokumentInfo("2.pdf", filId = UUID.nameUUIDFromBytes("2".toByteArray()).toString()),
                createMellomlagringDokumentInfo("3.pdf", filId = UUID.nameUUIDFromBytes("3".toByteArray()).toString()),
            )

        every { mellomlagringClient.hentDokumenterMetadata(any()) } returns
            createMellomlagringDto(
                metadataList = metadataList,
            )

        opprettDokumentasjon(
            soknadId = soknad.id,
            dokumenter =
                metadataList
                    .map {
                        DokumentRef(
                            UUID.fromString(it.filId),
                            it.filnavn,
                            getSha512FromByteArray(it.filnavn.toByteArray()),
                        )
                    }
                    .plus(DokumentRef(UUID.randomUUID(), "4.pdf", getSha512FromByteArray("4.pdf".toByteArray())))
                    .toSet(),
        ).also { dokumentasjonRepository.save(it) }

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it).hasSize(1)
            assertThat(it.first().dokumenter).hasSize(4)
        }

        documentValidator.validateDocumentsExistsInMellomlager(soknad.id)

        dokumentasjonRepository.findAllBySoknadId(soknad.id).also {
            assertThat(it).hasSize(1)
            assertThat(it.first().dokumenter).hasSize(3)
        }

        dokumentasjonRepository.deleteAll()
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

    private fun createMellomlagringDto(
        metadataList: List<MellomlagringDokumentInfo> = listOf(createMellomlagringDokumentInfo()),
    ): MellomlagringDto {
        return MellomlagringDto(
            navEksternRefId = soknad.id.toString(),
            mellomlagringMetadataList = metadataList,
        )
    }

    private fun createMellomlagringDokumentInfo(
        filnavn: String = pdfFil.name,
        filId: String = UUID.randomUUID().toString(),
    ) = MellomlagringDokumentInfo(
        filnavn = filnavn,
        filId = filId,
        storrelse = FileUtils.sizeOf(pdfFil),
        mimetype = FileDetectionUtils.detectMimeType(pdfFil.readBytes()),
    )

    private fun saveDokumentasjonAndReturnDokumentId(filId: UUID = UUID.randomUUID()): UUID {
        return opprettDokumentasjon(
            soknadId = soknad.id,
            status = DokumentasjonStatus.LASTET_OPP,
            dokumenter = setOf(DokumentRef(filId, pdfFil.name)),
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
            type: OpplysningType,
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
): DokumentRef? {
    return findAllBySoknadId(soknadId)
        .flatMap { it.dokumenter }
        .find { it.dokumentId == dokumentId }
}
