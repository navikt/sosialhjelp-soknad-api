package no.nav.sosialhjelp.soknad.vedlegg

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.DuplikatFilException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.CSV_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE_OLD
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.TEXT_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.WORD_FILE_OLD
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggService.Companion.MAKS_SAMLET_VEDLEGG_STORRELSE
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.getSha512FromByteArray
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.stream.ImageOutputStream
import javax.imageio.stream.MemoryCacheImageOutputStream

internal class OpplastetVedleggServiceTest {

    private val opplastetVedleggRepository: OpplastetVedleggRepository = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val virusScanner: VirusScanner = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()

    private val soknadUnderArbeidService =
        SoknadUnderArbeidService(soknadUnderArbeidRepository, kommuneInfoService)

    private val opplastetVedleggService =
        OpplastetVedleggService(opplastetVedleggRepository, soknadUnderArbeidRepository, soknadUnderArbeidService, virusScanner)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { virusScanner.scan(any(), any(), any(), any()) } just runs
        every { opplastetVedleggRepository.slettVedlegg(any(), any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun oppdatererVedleggStatusVedOpplastingAvVedlegg() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(VEDLEGGSTYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(VEDLEGGSTYPE).tilleggsinfo)
                            .withStatus("VedleggKreves")
                    )
                )
            )
        )
        every { opplastetVedleggRepository.opprettVedlegg(any(), any()) } returns "321"

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg =
            opplastetVedleggService.lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, imageFile, FILNAVN1)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val jsonVedlegg = soknadUnderArbeid.jsonInternalSoknad!!.vedlegg.vedlegg[0]
        assertThat(jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo).isEqualTo(VEDLEGGSTYPE)
        assertThat(jsonVedlegg.status).isEqualTo("LastetOpp")
        assertThat(jsonVedlegg.filer).hasSize(1)
        assertThat(opplastetVedlegg.filnavn.substring(0, 5)).isEqualTo(FILNAVN1.substring(0, 5))
    }

    @Test
    fun sletterVedleggStatusVedSlettingAvOpplastingAvVedlegg() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(VEDLEGGSTYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(VEDLEGGSTYPE).tilleggsinfo)
                            .withFiler(mutableListOf(JsonFiler().withFilnavn(FILNAVN2).withSha512(SHA512)))
                            .withStatus("LastetOpp")
                    )
                )
            )
        )
        every { opplastetVedleggRepository.hentVedlegg(any(), any()) } returns OpplastetVedlegg(
            eier = "eier",
            vedleggType = OpplastetVedleggType(VEDLEGGSTYPE),
            data = byteArrayOf(1, 2, 3),
            soknadId = 123L,
            filnavn = FILNAVN2,
            sha512 = SHA512
        )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(BEHANDLINGSID, "uuid")
        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val jsonVedlegg = soknadUnderArbeid.jsonInternalSoknad!!.vedlegg.vedlegg[0]
        assertThat(jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo).isEqualTo(VEDLEGGSTYPE)
        assertThat(jsonVedlegg.status).isEqualTo("VedleggKreves")
        assertThat(jsonVedlegg.filer).isEmpty()
    }

    @Test
    fun feilmeldingHvisSamletVedleggStorrelseOverskriderMaksgrense() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(VEDLEGGSTYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(VEDLEGGSTYPE).tilleggsinfo)
                            .withStatus("VedleggKreves")
                    )
                )
            )
        )
        every {
            opplastetVedleggRepository.hentSamletVedleggStorrelse(
                any(),
                any()
            )
        } returns MAKS_SAMLET_VEDLEGG_STORRELSE

        val imageFile = createByteArrayFromJpeg()
        assertThatExceptionOfType(SamletVedleggStorrelseForStorException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .sjekkOmSoknadUnderArbeidTotalVedleggStorrelseOverskriderMaksgrense(BEHANDLINGSID, imageFile)
            }
    }

    @Test
    fun feilmeldingHvisFiltypeErUgyldigMenValidererMedTika() {
        val imageFile = createByteArrayFromJpeg()
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, imageFile, "filnavn.jfif")
            }
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, imageFile, "filnavn.pjpeg")
            }
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, imageFile, "filnavn.pjp")
            }
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .lastOppVedlegg(
                        BEHANDLINGSID,
                        VEDLEGGSTYPE,
                        "ikkeBildeEllerPdf".toByteArray(),
                        "filnavnUtenFiltype"
                    )
            }
    }

    @Test
    fun skalUtvideFilnavnHvisTikaValidererOkMenFilExtensionMangler() {
        doCommonMocking()

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg = opplastetVedleggService
            .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, imageFile, "filnavnUtenFiltype")
        assertThat(opplastetVedlegg.filnavn).startsWith("filnavnUtenFiltype").endsWith(".jpg")
    }

    @Test
    fun skalUtvideFilnavnHvisTikaValidererOkMenFilnavnInneholderPunktumUtenGyldigFilExtension() {
        doCommonMocking()

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg = opplastetVedleggService.lastOppVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE,
            imageFile,
            "filnavnMed.punktum"
        )
        assertThat(opplastetVedlegg.filnavn).startsWith("filnavnMedpunktum").endsWith(".jpg")
    }

    @Test
    fun skalEndreFilExtensionHvisTikaValidererSomNoeAnnetEnnFilnavnetTilsier() {
        doCommonMocking()

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg =
            opplastetVedleggService.lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, imageFile, "filnavn.pdf")
        assertThat(opplastetVedlegg.filnavn).startsWith("filnavn").endsWith(".jpg")
    }

    @Test
    fun `Opplasting av Excel-fil kaster ikke exception`() {
        doCommonMocking()

        val filename = EXCEL_FILE.let { it.name.substring(0, it.name.indexOf(".")) }
        val opplastetVedlegg = opplastetVedleggService.lastOppVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE,
            EXCEL_FILE.readBytes(),
            EXCEL_FILE.name
        )
        assertThat(opplastetVedlegg.filnavn).startsWith(filename).endsWith(".pdf")
    }

    @Test
    fun `Opplasting av Word-fil kaster ikke exception`() {
        doCommonMocking()

        val filename = WORD_FILE.let { it.name.substring(0, it.name.indexOf(".")) }
        val opplastetVedlegg = opplastetVedleggService.lastOppVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE,
            WORD_FILE.readBytes(),
            WORD_FILE.name
        )
        assertThat(opplastetVedlegg.filnavn).startsWith(filename).endsWith(".pdf")
    }

    @Test
    fun `Opplasting av CSV-fil kaster ikke exception`() {
        doCommonMocking()

        val filename = CSV_FILE.let { it.name.substring(0, it.name.indexOf(".")) }
        val opplastetVedlegg = opplastetVedleggService.lastOppVedlegg(
            BEHANDLINGSID,
            VEDLEGGSTYPE,
            CSV_FILE.readBytes(),
            CSV_FILE.name
        )
        assertThat(opplastetVedlegg.filnavn).startsWith(filename).endsWith(".pdf")
    }

    @Test
    fun `Skal ikke kunne laste opp gammelt Excel-format`() {
        assertThatThrownBy {
            opplastetVedleggService
                .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, EXCEL_FILE_OLD.readBytes(), EXCEL_FILE_OLD.name)
        }
            .isInstanceOf(UgyldigOpplastingTypeException::class.java)
            .hasMessageContaining("Ugyldig filtype for opplasting")
            .hasMessageContaining(detectMimeType(EXCEL_FILE_OLD.readBytes()))
    }

    @Test
    fun `Skal ikke kunne laste opp gammelt Word-format`() {
        assertThatThrownBy {
            opplastetVedleggService
                .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, WORD_FILE_OLD.readBytes(), WORD_FILE_OLD.name)
        }
            .isInstanceOf(UgyldigOpplastingTypeException::class.java)
            .hasMessageContaining("Ugyldig filtype for opplasting")
            .hasMessageContaining(detectMimeType(WORD_FILE_OLD.readBytes()))
    }

    @Test
    fun `Vanlig tekst-fil konverteres ikke`() {
        assertThatThrownBy {
            opplastetVedleggService
                .lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, TEXT_FILE.readBytes(), TEXT_FILE.name)
        }
            .isInstanceOf(UgyldigOpplastingTypeException::class.java)
            .hasMessageContaining("Ugyldig filtype for opplasting")
            .hasMessageContaining(detectMimeType(TEXT_FILE.readBytes()))
    }

    @Test
    fun `Skal oppdatere JsonInternalSoknad med vedleggsinformasjon`() {
        val opplastetVedlegg = OpplastetVedlegg(
            eier = SubjectHandlerUtils.getUserIdFromToken(),
            vedleggType = OpplastetVedleggType(VEDLEGGSTYPE),
            data = PDF_FILE.readBytes(),
            soknadId = 1L,
            filnavn = PDF_FILE.name
        )
        every { opplastetVedleggRepository.opprettVedlegg(opplastetVedlegg, any()) } returns UUID.nameUUIDFromBytes(
            PDF_FILE.readBytes()
        ).toString()

        val initSoknadUnderArbeid = createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(VEDLEGGSTYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(VEDLEGGSTYPE).tilleggsinfo)
                            .withStatus("VedleggKreves")
                    )
                )
            )
        )
        every { soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, any()) } returns initSoknadUnderArbeid
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        soknadUnderArbeidService.oppdaterSoknadUnderArbeid(
            opplastetVedlegg.sha512,
            BEHANDLINGSID,
            "hei|på deg",
            opplastetVedlegg.filnavn
        )

        val soknadUnderArbeid = slot.captured
        val fil = soknadUnderArbeid.jsonInternalSoknad!!.vedlegg!!.vedlegg[0].filer[0]

        assertThat(fil.sha512).isEqualTo(getSha512FromByteArray(PDF_FILE.readBytes()))
        assertThat(fil.filnavn.contains(PDF_FILE.name.split(".")[0]))
    }

    @Test
    fun `Laste opp samme fil 2 ganger skal gi feil`() {
        doCommonMocking()

        opplastetVedleggService.lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, PDF_FILE.readBytes(), PDF_FILE.name)
        // skal feile andre runde
        assertThatThrownBy { opplastetVedleggService.lastOppVedlegg(BEHANDLINGSID, VEDLEGGSTYPE, PDF_FILE.readBytes(), PDF_FILE.name) }
            .isInstanceOf(DuplikatFilException::class.java)
    }

    private fun doCommonMocking() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(VEDLEGGSTYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(VEDLEGGSTYPE).tilleggsinfo)
                            .withStatus("VedleggKreves")
                    )
                )
            )
        )
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { opplastetVedleggRepository.opprettVedlegg(any(), any()) } returns "321"
    }

    private fun createByteArrayFromJpeg(): ByteArray {
        val bf = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val imageOutputStream: ImageOutputStream = MemoryCacheImageOutputStream(byteArrayOutputStream)
        ImageIO.write(bf, "jpg", imageOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val FILNAVN1 = "Bifil.jpeg"
        private const val FILNAVN2 = "Homofil.png"
        private const val SHA512 = "Shakk matt"
        private const val VEDLEGGSTYPE = "hei|på deg"

        private fun createSoknadUnderArbeid(jsonInternalSoknad: JsonInternalSoknad): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = BEHANDLINGSID,
                tilknyttetBehandlingsId = null,
                eier = "EIER",
                jsonInternalSoknad = jsonInternalSoknad,
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
