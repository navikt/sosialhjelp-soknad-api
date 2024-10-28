package no.nav.sosialhjelp.soknad.vedlegg

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRowMapper
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SenderUtils.createPrefixedBehandlingsId
import no.nav.sosialhjelp.soknad.innsending.digisosapi.dto.FilOpplasting
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.v2.shadow.DokumentasjonAdapter
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.DokumentUploadUnsupportedMediaType
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringClient
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDokumentInfo
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringDto
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

// eksplisitt Transactional fordi vi bruker JdbcTemplate og de "gamle" repository-klassene (som også bruker jdbcTemplate)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
class OpplastetVedleggServiceTest {
    @Autowired
    private lateinit var opplastetVedleggService: OpplastetVedleggService

    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockkBean
    private lateinit var virusScanner: VirusScanner

    @MockkBean
    private lateinit var mellomlagringClient: MellomlagringClient

    @MockkBean
    private lateinit var dokumentasjonAdapter: DokumentasjonAdapter

    @BeforeEach
    fun setup() {
        mockkObject(SubjectHandlerUtils)
        every { SubjectHandlerUtils.getUserIdFromToken() } returns EIER
        every { virusScanner.scan(any(), any(), any(), any()) } just runs
        every { dokumentasjonAdapter.saveDokumentMetadata(any(), any(), any(), any(), any()) } just runs
    }

    @Test
    internal fun `Skal oppdatere soknad_under_arbeid med filer i vedlegg hvis ingenting feiler mot Fiks mellomlagring`() {
        val filOpplastingCapturingSlot = slot<FilOpplasting>()
        every { mellomlagringClient.postVedlegg(any(), capture(filOpplastingCapturingSlot)) } returns createMellomlagringDto()

        soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)

        val (nyttFilnavn, _) =
            opplastetVedleggService.uploadDocument(
                behandlingsId = BEHANDLINGSID,
                dokumentasjonType = DOKUMENTASJONSTYPE.stringName,
                orginaltFilnavn = ORIGINALT_FILNAVN,
                data = PDF_FILE.readBytes(),
            )

        val soknadUnderArbeidFraDb =
            jdbcTemplate.query(
                "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
                SoknadUnderArbeidRowMapper(),
                BEHANDLINGSID,
            ).firstOrNull()

        assertThat(soknadUnderArbeidFraDb?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.filer?.size).isEqualTo(1)

        assertThat(
            soknadUnderArbeidFraDb?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.filer?.get(0)?.filnavn,
        ).isEqualTo(nyttFilnavn)
    }

    @Test
    internal fun `Skal ikke oppdatere soknadUnderArbeid med filer i vedlegg hvis feil kaller mot FIKS`() {
        every { mellomlagringClient.postVedlegg(any(), any()) } throws (IllegalStateException("feil"))

        soknadUnderArbeidRepository.opprettSoknad(
            lagSoknadUnderArbeid(BEHANDLINGSID),
            EIER,
        )

        assertThatThrownBy {
            opplastetVedleggService.uploadDocument(
                behandlingsId = BEHANDLINGSID,
                dokumentasjonType = DOKUMENTASJONSTYPE.stringName,
                orginaltFilnavn = ORIGINALT_FILNAVN,
                data = PDF_FILE.readBytes(),
            )
        }.isInstanceOf(IllegalStateException::class.java)

        val soknadUnderArbeidFraDb =
            jdbcTemplate.query(
                "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
                SoknadUnderArbeidRowMapper(),
                BEHANDLINGSID,
            ).firstOrNull()

        assertThat(soknadUnderArbeidFraDb?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.filer?.size).isEqualTo(0)
    }

    @Test
    fun `Test uploade fil som ikke stottes`() {
        val behandlingsId = UUID.randomUUID().toString()
        val eksternId = createPrefixedBehandlingsId(behandlingsId)

        MellomlagringDto(eksternId, emptyList())
            .also {
                every { mellomlagringClient.postVedlegg(eksternId, any()) } returns it
            }

        assertThatThrownBy {
            opplastetVedleggService.uploadDocument(
                behandlingsId = behandlingsId,
                dokumentasjonType = "hei|på deg",
                orginaltFilnavn = EXCEL_FILE.name,
                data = EXCEL_FILE_OLD.readBytes(),
            )
        }
            .isInstanceOf(DokumentUploadUnsupportedMediaType::class.java)
            .hasMessageContaining("Ugyldig filtype for opplasting")
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            eier = EIER,
            jsonInternalSoknad = lagInternalSoknadJson(),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO,
        )
    }

    private fun createMellomlagringDto() =
        MellomlagringDto(
            navEksternRefId = BEHANDLINGSID,
            mellomlagringMetadataList =
                listOf(
                    MellomlagringDokumentInfo(filnavn = FILNAVN, filId = "uuid", storrelse = 123L, mimetype = "mime"),
                ),
        )

    companion object {
        private const val EIER = "12345678901"
        private const val FILNAVN = "EksempelPDF_123"
        private const val ORIGINALT_FILNAVN = "EksempelPDF"
        private val DOKUMENTASJONSTYPE = VedleggType.AnnetAnnet
        private val BEHANDLINGSID = UUID.randomUUID().toString()
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SIST_ENDRET_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    }
}

private fun lagInternalSoknadJson(): JsonInternalSoknad {
    return JsonInternalSoknad()
        .withVedlegg(
            JsonVedleggSpesifikasjon().withVedlegg(
                listOf(
                    JsonVedlegg()
                        .withType("annet")
                        .withTilleggsinfo("annet")
                        .withStatus("VedleggKreves"),
                ),
            ),
        )
}
