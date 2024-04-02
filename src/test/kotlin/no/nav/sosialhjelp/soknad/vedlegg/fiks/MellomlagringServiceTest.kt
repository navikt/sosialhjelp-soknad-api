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
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
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
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
    internal fun `skal returnere tom liste når det ikke finnes innslag for behandlingsid i mellomlager`() {

        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns null
        assertThat(mellomlagringService.getAllVedlegg("behandlingsId")).isEmpty()
    }

    @Test
    internal fun `skal returnere tom liste når det ikke finnes vedlegg for gitt behandlingsid`() {

        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = null
        )
        assertThat(mellomlagringService.getAllVedlegg("behandlingsId")).isEmpty()
    }

    @Test
    internal fun `skal returnere liste med vedlegg når det finnes mellomlagrede vedlegg`() {

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
    internal fun `skal ikke finne vedlegg for id dersom behandlingsid ikke finnes i mellomlager`() {

        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns null
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }
    }

    @Test
    internal fun `skal returnere 0 vedlegg dersom det ikke ligger vedlegg for behandlingsid i mellomlager`() {

        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = null
        )
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }
    }

    @Test
    internal fun `skal returnere 0 vedlegg dersom vedleggid ikke finnes i liste over vedlegg`() {

        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = "behandlingsId",
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = "filnavn", filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        assertThat(mellomlagringService.getVedlegg("behandlingsId", "vedleggId")).isNull()
        verify(exactly = 0) { mellomlagringClient.getVedlegg(any(), any()) }
    }

    @Test
    internal fun `skal returnere riktig vedlegg dersom vedleggsid finnes for behandlingsid i mellomlager`() {

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
        every { mellomlagringClient.getMellomlagredeVedlegg(eksternId) } returns MellomlagringDto(
            eksternId,
            emptyList()
        )

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
                            .withType("hei")
                            .withTilleggsinfo("på deg")
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

    private fun createSoknadUnderArbeid(
        behandligsId: String,
        jsonInternalSoknad: JsonInternalSoknad
    ): SoknadUnderArbeid {
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
