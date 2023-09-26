package no.nav.sosialhjelp.soknad.vedlegg.fiks

import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import jakarta.inject.Inject
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRowMapper
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
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

    @Inject
    private lateinit var mellomlagringClient: MellomlagringClient

    @Inject
    private lateinit var virusScanner: VirusScanner
    private val soknadUnderArbeidRowMapper = SoknadUnderArbeidRowMapper()

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Inject
    private lateinit var soknadUnderArbeidRepository: SoknadUnderArbeidRepository

    @Inject
    private lateinit var opplastetVedleggRepository: OpplastetVedleggRepository

    @Inject
    private lateinit var soknadUnderArbeidService: SoknadUnderArbeidService

    @Inject
    lateinit var mellomlagringService: MellomlagringService

    @BeforeEach
    fun setUp() {

        mockkObject(SubjectHandlerUtils)
        mockkObject(VedleggUtils)
        every { SubjectHandlerUtils.getUserIdFromToken() } returns EIER
        every { VedleggUtils.behandleFilOgReturnerFildata(any(), any()) } returns Pair(FILNAVN, PDF_FILE.readBytes())
        every { virusScanner.scan(any(), any(), any(), any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from OPPLASTET_VEDLEGG")
        jdbcTemplate.update("delete from SOKNAD_UNDER_ARBEID")
    }

    @Test
    internal fun `skal oppdatere soknad_under_arbeid med vedlegg hvis ingenting feiler mot Fiks mellomlagring`() {

        every { mellomlagringClient.postVedlegg(any(), any()) } just runs
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = BEHANDLINGSID,
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = FILNAVN, filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)

        mellomlagringService.uploadVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE.stringName,
            PDF_FILE.readBytes(),
            ORIGINALT_FILNAVN
        )

        val retur = jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            BEHANDLINGSID
        ).firstOrNull()

        assertThat(retur?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(retur?.jsonInternalSoknad?.vedlegg?.vedlegg?.size).isEqualTo(1)
        assertThat(retur?.jsonInternalSoknad?.vedlegg?.vedlegg?.get(0)?.type).isEqualTo("annet")
    }

    @Test
    internal fun `skal ikke oppdatere soknad_under_arbeid med vedlegg hvis feil kaller mot FIKS`() {

        every { mellomlagringClient.postVedlegg(any(), any()) } throws (IllegalStateException("feil"))
        every { mellomlagringClient.getMellomlagredeVedlegg(any()) } returns MellomlagringDto(
            navEksternRefId = BEHANDLINGSID,
            mellomlagringMetadataList = listOf(
                MellomlagringDokumentInfo(filnavn = FILNAVN, filId = "uuid", storrelse = 123L, mimetype = "mime")
            )
        )
        soknadUnderArbeidRepository.opprettSoknad(lagSoknadUnderArbeid(BEHANDLINGSID), EIER)

        mellomlagringService.uploadVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE.stringName,
            PDF_FILE.readBytes(),
            ORIGINALT_FILNAVN
        )

        val retur = jdbcTemplate.query(
            "select * from SOKNAD_UNDER_ARBEID where BEHANDLINGSID = ?",
            soknadUnderArbeidRowMapper,
            BEHANDLINGSID
        ).firstOrNull()

        assertThat(retur?.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(retur?.jsonInternalSoknad?.vedlegg?.vedlegg?.size).isEqualTo(0)
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
            // .withSoknad(
            //     JsonSoknad()
            //         .withVersion("1.0.0")
            //         .withDriftsinformasjon(
            //             JsonDriftsinformasjon()
            //                 .withUtbetalingerFraNavFeilet(false)
            //                 .withInntektFraSkatteetatenFeilet(false)
            //                 .withStotteFraHusbankenFeilet(false))
            //         .withMottaker(JsonSoknadsmottaker())
            //         .withData(
            //             JsonData()
            //                 .withPersonalia(
            //                     JsonPersonalia().withPersonIdentifikator(
            //                         JsonPersonIdentifikator()
            //                             .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
            //                             .withVerdi(EIER)
            //                     )
            //                         .withNavn(
            //                             JsonSokernavn()
            //                                 .withFornavn("Navn")
            //                                 .withMellomnavn("")
            //                                 .withEtternavn("Navnesen")
            //                                 .withKilde(JsonSokernavn.Kilde.SYSTEM)
            //                         )
            //                         .withKontonummer(
            //                             JsonKontonummer()
            //                                 .withKilde(JsonKilde.SYSTEM)
            //                                 .withVerdi("12345678901")
            //                         )
            //                 )
            //                 .withArbeid(JsonArbeid())
            //                 .withUtdanning(
            //                     JsonUtdanning()
            //                         .withKilde(JsonKilde.SYSTEM)
            //                 )
            //                 .withFamilie(
            //                     JsonFamilie()
            //                         .withForsorgerplikt(JsonForsorgerplikt())
            //                 )
            //                 .withBegrunnelse(
            //                     JsonBegrunnelse()
            //                         .withKilde(JsonKildeBruker.BRUKER)
            //                         .withHvaSokesOm("")
            //                         .withHvorforSoke("")
            //                 )
            //                 .withBosituasjon(
            //                     JsonBosituasjon()
            //                         .withKilde(JsonKildeBruker.BRUKER)
            //                 )
            //                 .withOkonomi(
            //                     JsonOkonomi()
            //                         .withOpplysninger(
            //                             JsonOkonomiopplysninger()
            //                                 .withUtbetaling(emptyList())
            //                                 .withUtgift(emptyList())
            //                         )
            //                         .withOversikt(
            //                             JsonOkonomioversikt()
            //                                 .withInntekt(emptyList())
            //                                 .withUtgift(emptyList())
            //                                 .withFormue(emptyList())
            //                         )
            //                 )
            //         )
            // )
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

    companion object {
        private const val EIER = "12345678901"
        private const val FILNAVN = "EksempelPDF_123"
        private const val ORIGINALT_FILNAVN = "EksempelPDF"
        private val VEDLEGGSTYPE = VedleggType.AnnetAnnet

        // private const val EIER2 = "22222222222"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SIST_ENDRET_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    }
}
