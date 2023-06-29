package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.DuplikatFilException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import java.util.UUID.nameUUIDFromBytes as uuidFromBytes

internal class MellomlagringServiceTest {

    private val mellomlagringClient: MellomlagringClient = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val virusScanner: VirusScanner = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()

    private val soknadUnderArbeidService = SoknadUnderArbeidService(
        soknadUnderArbeidRepository, kommuneInfoService
    )

    private val mellomlagringService = MellomlagringService(
        mellomlagringClient,
        soknadUnderArbeidService,
        virusScanner
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.environmentName } returns "test"
        every { MiljoUtils.isNonProduction() } returns true

        every { virusScanner.scan(any(), any(), any(), any()) } just runs

        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
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
    fun `Test uploade fil som ikke stottes`() {

        val behandlingsId = "123"
        val eksternId = createPrefixedBehandlingsId(behandlingsId)

        every { mellomlagringClient.postVedlegg(eksternId, any()) } just runs
        every { mellomlagringClient.getMellomlagredeVedlegg(eksternId) } returns MellomlagringDto(eksternId, emptyList())

        assertThatThrownBy {
            mellomlagringService.uploadVedlegg(behandlingsId, "hei|på deg", EXCEL_FILE_OLD.readBytes(), EXCEL_FILE.name)
        }
            .isInstanceOf(UgyldigOpplastingTypeException::class.java)
            .hasMessageContaining("Ugyldig filtype for opplasting")
    }

    @Test
    fun `Test skal oppdatere JsonInternalSoknad med vedleggsinfo`() {
        val behandlingsId = "123"

        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns createSoknadUnderArbeid(
            behandlingsId,
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType("hei|på deg").type)
                            .withTilleggsinfo(OpplastetVedleggType("hei|på deg").tilleggsinfo)
                            .withStatus("VedleggKreves")
                    )
                )
            )
        )
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        soknadUnderArbeidService.oppdaterSoknadUnderArbeid(
            getSha512FromByteArray(PDF_FILE.readBytes()),
            behandlingsId,
            "hei|på deg",
            PDF_FILE.name
        )

        val soknadUnderArbeid = slot.captured
        val vedlegg = soknadUnderArbeid.jsonInternalSoknad!!.vedlegg!!.vedlegg[0]
        val fil = vedlegg.filer[0]

        assertThat(fil.sha512).isEqualTo(getSha512FromByteArray(PDF_FILE.readBytes()))
        assertThat(fil.filnavn).contains(PDF_FILE.name.split(".")[0])
    }

    @Test
    fun `Last opp fil kaster ikke exception`() {

        val behandlingsId = "123"
        val eksternId = createPrefixedBehandlingsId(behandlingsId)

        val soknadUnderArbeid = createSoknadUnderArbeid(
            behandlingsId,
            createJsonInternalSoknad()
        )

        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        every { mellomlagringClient.postVedlegg(eksternId, any()) } just runs
        every { mellomlagringClient.getMellomlagredeVedlegg(eksternId) } returns MellomlagringDto(
            eksternId,
            createDokumentInfoList(PDF_FILE)
        )
        mellomlagringService.uploadVedlegg(behandlingsId, "hei|på deg", PDF_FILE.readBytes(), PDF_FILE.name)
    }

    @Test
    fun `Allerede opplastet fil kaster exception`() {

        val behandlingsId = "123"
        val eksternId = createPrefixedBehandlingsId(behandlingsId)

        val soknadUnderArbeid = createSoknadUnderArbeid(
            behandlingsId,
            createJsonInternalSoknad()
        )

        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        every { mellomlagringClient.postVedlegg(eksternId, any()) } just runs
        every { mellomlagringClient.getMellomlagredeVedlegg(eksternId) } returns MellomlagringDto(
            eksternId,
            createDokumentInfoList(PDF_FILE)
        )

        mellomlagringService.uploadVedlegg(behandlingsId, "hei|på deg", PDF_FILE.readBytes(), PDF_FILE.name)
        assertThatThrownBy {
            mellomlagringService.uploadVedlegg(behandlingsId, "hei|på deg", PDF_FILE.readBytes(), PDF_FILE.name)
        }.isInstanceOf(DuplikatFilException::class.java)
    }

    private fun createJsonInternalSoknad(): JsonInternalSoknad {
        return JsonInternalSoknad().withVedlegg(
            JsonVedleggSpesifikasjon().withVedlegg(
                listOf(
                    JsonVedlegg()
                        .withType(OpplastetVedleggType("hei|på deg").type)
                        .withTilleggsinfo(OpplastetVedleggType("hei|på deg").tilleggsinfo)
                        .withStatus("VedleggKreves")
                )
            )
        )
    }

    private fun createDokumentInfoList(fil: File): List<MellomlagringDokumentInfo> {
        val bytes = fil.readBytes()
        return listOf(
            MellomlagringDokumentInfo(
                lagFilnavn(fil.name, validerFil(bytes, fil.name), uuidFromBytes(bytes)),
                uuidFromBytes(bytes).toString(),
                bytes.size.toLong(),
                detectMimeType(bytes)
            )
        )
    }

    private fun createSoknadUnderArbeid(behandligsId: String, jsonInternalSoknad: JsonInternalSoknad): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandligsId,
            tilknyttetBehandlingsId = null,
            eier = "EIER",
            jsonInternalSoknad = jsonInternalSoknad,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }
}
