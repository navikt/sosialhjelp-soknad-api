package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import org.springframework.beans.factory.annotation.Autowired
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRowMapper
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [MellomLagringServiceTestConfig::class])
@ActiveProfiles("test")
internal class MellomLagringServiceUtenMocketRepositoryTest {

    @Autowired
    private lateinit var mellomlagringClient: MellomlagringClient

    @Autowired
    private lateinit var mellomlagringService: MellomlagringService

    @Autowired
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Autowired
    private lateinit var virusScanner: VirusScanner

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val soknadUnderArbeidRowMapper = SoknadUnderArbeidRowMapper()

    @BeforeEach
    fun setUp() {

        mockkObject(SubjectHandlerUtils)
        mockkObject(VedleggUtils)
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns lagMellomlagringDto()
        every { SubjectHandlerUtils.getUserIdFromToken() } returns EIER
        every { VedleggUtils.behandleFilOgReturnerFildata(any(), any()) } returns Pair(FILNAVN, PDF_FILE.readBytes())
        every { virusScanner.scan(any(), any(), any(), any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
        unmockkObject(SubjectHandlerUtils)
        unmockkObject(VedleggUtils)
    }

    @Test
    internal fun `skal oppdatere soknad_under_arbeid med filer i vedlegg hvis ingenting feiler mot Fiks mellomlagring`() {

        every { mellomlagringClient.postVedlegg(any(), any()) } just runs

        soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)

        mellomlagringService.uploadVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE.stringName,
            PDF_FILE.readBytes(),
            ORIGINALT_FILNAVN
        )

        val soknadUnderArbeidFraDb = jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            BEHANDLINGSID
        ).firstOrNull()

        assertThat(soknadUnderArbeidFraDb?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.filer?.size).isEqualTo(1)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.filer?.get(0)?.filnavn).isEqualTo(
            FILNAVN
        )
    }

    @Test
    internal fun `skal ikke oppdatere soknad_under_arbeid med filer i vedlegg hvis feil kaller mot FIKS`() {

        every { mellomlagringClient.postVedlegg(any(), any()) } throws (IllegalStateException("feil"))

        soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)

        assertThatThrownBy {
            mellomlagringService.uploadVedlegg(
                BEHANDLINGSID,
                VEDLEGGSTYPE.stringName,
                PDF_FILE.readBytes(),
                ORIGINALT_FILNAVN
            )
        }

        val soknadUnderArbeidFraDb = jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            BEHANDLINGSID
        ).firstOrNull()

        assertThat(soknadUnderArbeidFraDb?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(soknadUnderArbeidFraDb?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.filer?.size).isEqualTo(0)
    }

    private fun lagSoknadUnderArbeid(behandlingsId: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = lagInternalSoknadJson(),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO
        )
    }

    private fun lagInternalSoknadJson(): JsonInternalSoknad {
        return JsonInternalSoknad()
            .withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType("annet")
                            .withTilleggsinfo("annet")
                            .withStatus("VedleggKreves")
                    )
                )
            )
    }

    private fun lagMellomlagringDto(): MellomlagringDto {
        return MellomlagringDto(
            navEksternRefId = BEHANDLINGSID,
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = FILNAVN, filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val FILNAVN = "EksempelPDF_123"
        private const val ORIGINALT_FILNAVN = "EksempelPDF"
        private val VEDLEGGSTYPE = VedleggType.AnnetAnnet
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SIST_ENDRET_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    }
}
