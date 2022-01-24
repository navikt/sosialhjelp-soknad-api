package no.nav.sosialhjelp.soknad.vedlegg

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.filedetection.TikaFileType.JPEG
import no.nav.sosialhjelp.soknad.common.filedetection.TikaFileType.PNG
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.VedleggType
import no.nav.sosialhjelp.soknad.vedlegg.OpplastetVedleggService.Companion.MAKS_SAMLET_VEDLEGG_STORRELSE
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.SamletVedleggStorrelseForStorException
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.virusscan.VirusScanner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Optional
import javax.imageio.ImageIO
import javax.imageio.stream.ImageOutputStream
import javax.imageio.stream.MemoryCacheImageOutputStream

internal class OpplastetVedleggServiceTest {

    private val opplastetVedleggRepository: OpplastetVedleggRepository = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val virusScanner: VirusScanner = mockk()

    private val opplastetVedleggService =
        OpplastetVedleggService(opplastetVedleggRepository, soknadUnderArbeidRepository, virusScanner)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        clearAllMocks()
        every { virusScanner.scan(any(), any(), any(), any()) } just runs
        every { opplastetVedleggRepository.slettVedlegg(any(), any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        System.clearProperty("environment.name")
    }

    @Test
    fun lagerFilnavn() {
        val filnavn = opplastetVedleggService.lagFilnavn("minfil.jpg", JPEG, "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99")
        assertThat(filnavn).isEqualTo("minfil-5c2a1cea.jpg")

        val truncate = opplastetVedleggService.lagFilnavn(
            "etkjempelangtfilnavn12345678901234567890123456789012345678901234567890.jpg",
            JPEG,
            "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99"
        )
        assertThat(truncate).isEqualTo("etkjempelangtfilnavn123456789012345678901234567890-5c2a1cea.jpg")

        val medSpesialTegn = opplastetVedleggService.lagFilnavn("en.filmedææå()ogmyerartsjø.jpg", JPEG, "abc-ef05")
        assertThat(medSpesialTegn).isEqualTo("enfilmedeeaogmyerartsjo-abc.jpg")

        val utenExtension = opplastetVedleggService.lagFilnavn("minfil", PNG, "abc-ef05")
        assertThat(utenExtension).isEqualTo("minfil-abc.png")

        val forskjelligExtension = opplastetVedleggService.lagFilnavn("minfil.jpg", PNG, "abc-ef05")
        assertThat(forskjelligExtension).isEqualTo("minfil-abc.png")

        val caseInsensitiveExtension = opplastetVedleggService.lagFilnavn("minfil.JPG", JPEG, "abc-ef05")
        assertThat(caseInsensitiveExtension).isEqualTo("minfil-abc.JPG")
    }

    @Test
    fun oppdatererVedleggStatusVedOpplastingAvVedlegg() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns SoknadUnderArbeid()
            .withJsonInternalSoknad(
                JsonInternalSoknad().withVedlegg(
                    JsonVedleggSpesifikasjon().withVedlegg(
                        listOf(
                            JsonVedlegg()
                                .withType(VedleggType(TYPE).type)
                                .withTilleggsinfo(VedleggType(TYPE).tilleggsinfo)
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
            opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, FILNAVN1)
        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val jsonVedlegg = soknadUnderArbeid.jsonInternalSoknad.vedlegg.vedlegg[0]
        assertThat(jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo).isEqualTo(TYPE)
        assertThat(jsonVedlegg.status).isEqualTo("LastetOpp")
        assertThat(jsonVedlegg.filer).hasSize(1)
        assertThat(opplastetVedlegg.uuid).isEqualTo("321")
        assertThat(opplastetVedlegg.filnavn.substring(0, 5)).isEqualTo(FILNAVN1.substring(0, 5))
    }

    @Test
    fun sletterVedleggStatusVedSlettingAvOpplastingAvVedlegg() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns SoknadUnderArbeid()
            .withJsonInternalSoknad(
                JsonInternalSoknad().withVedlegg(
                    JsonVedleggSpesifikasjon().withVedlegg(
                        listOf(
                            JsonVedlegg()
                                .withType(VedleggType(TYPE).type)
                                .withTilleggsinfo(VedleggType(TYPE).tilleggsinfo)
                                .withFiler(mutableListOf(JsonFiler().withFilnavn(FILNAVN2).withSha512(SHA512)))
                                .withStatus("LastetOpp")
                        )
                    )
                )
            )
        every { opplastetVedleggRepository.hentVedlegg(any(), any()) } returns Optional.of(
            OpplastetVedlegg().withVedleggType(VedleggType(TYPE)).withFilnavn(FILNAVN2).withSha512(SHA512)
        )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        opplastetVedleggService.deleteVedleggAndUpdateVedleggstatus(BEHANDLINGSID, "uuid")
        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val jsonVedlegg = soknadUnderArbeid.jsonInternalSoknad.vedlegg.vedlegg[0]
        assertThat(jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo).isEqualTo(TYPE)
        assertThat(jsonVedlegg.status).isEqualTo("VedleggKreves")
        assertThat(jsonVedlegg.filer).isEmpty()
    }

    @Test
    fun feilmeldingHvisSamletVedleggStorrelseOverskriderMaksgrense() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns SoknadUnderArbeid()
            .withJsonInternalSoknad(
                JsonInternalSoknad().withVedlegg(
                    JsonVedleggSpesifikasjon().withVedlegg(
                        listOf(
                            JsonVedlegg()
                                .withType(VedleggType(TYPE).type)
                                .withTilleggsinfo(VedleggType(TYPE).tilleggsinfo)
                                .withStatus("VedleggKreves")
                        )
                    )
                )
            )
            .withSoknadId(SOKNAD_ID)
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
                    .saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.jfif")
            }
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.pjpeg")
            }
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.pjp")
            }
        assertThatExceptionOfType(UgyldigOpplastingTypeException::class.java)
            .isThrownBy {
                opplastetVedleggService
                    .saveVedleggAndUpdateVedleggstatus(
                        BEHANDLINGSID,
                        TYPE,
                        "ikkeBildeEllerPdf".toByteArray(),
                        "filnavnUtenFiltype"
                    )
            }
    }

    @Test
    fun skalUtvideFilnavnHvisTikaValidererOkMenFilExtensionMangler() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns SoknadUnderArbeid()
            .withJsonInternalSoknad(
                JsonInternalSoknad().withVedlegg(
                    JsonVedleggSpesifikasjon().withVedlegg(
                        listOf(
                            JsonVedlegg()
                                .withType(VedleggType(TYPE).type)
                                .withTilleggsinfo(VedleggType(TYPE).tilleggsinfo)
                                .withStatus("VedleggKreves")
                        )
                    )
                )
            )
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { opplastetVedleggRepository.opprettVedlegg(any(), any()) } returns "321"

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg = opplastetVedleggService
            .saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavnUtenFiltype")
        assertThat(opplastetVedlegg.filnavn).startsWith("filnavnUtenFiltype").endsWith(".jpg")
    }

    @Test
    fun skalUtvideFilnavnHvisTikaValidererOkMenFilnavnInneholderPunktumUtenGyldigFilExtension() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns SoknadUnderArbeid()
            .withJsonInternalSoknad(
                JsonInternalSoknad().withVedlegg(
                    JsonVedleggSpesifikasjon().withVedlegg(
                        listOf(
                            JsonVedlegg()
                                .withType(VedleggType(TYPE).type)
                                .withTilleggsinfo(VedleggType(TYPE).tilleggsinfo)
                                .withStatus("VedleggKreves")
                        )
                    )
                )
            )
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { opplastetVedleggRepository.opprettVedlegg(any(), any()) } returns "321"

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg = opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(
            BEHANDLINGSID,
            TYPE,
            imageFile,
            "filnavnMed.punktum"
        )
        assertThat(opplastetVedlegg.filnavn).startsWith("filnavnMedpunktum").endsWith(".jpg")
    }

    @Test
    fun skalEndreFilExtensionHvisTikaValidererSomNoeAnnetEnnFilnavnetTilsier() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns SoknadUnderArbeid()
            .withJsonInternalSoknad(
                JsonInternalSoknad().withVedlegg(
                    JsonVedleggSpesifikasjon().withVedlegg(
                        listOf(
                            JsonVedlegg()
                                .withType(VedleggType(TYPE).type)
                                .withTilleggsinfo(VedleggType(TYPE).tilleggsinfo)
                                .withStatus("VedleggKreves")
                        )
                    )
                )
            )
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { opplastetVedleggRepository.opprettVedlegg(any(), any()) } returns "321"

        val imageFile = createByteArrayFromJpeg()
        val opplastetVedlegg =
            opplastetVedleggService.saveVedleggAndUpdateVedleggstatus(BEHANDLINGSID, TYPE, imageFile, "filnavn.pdf")
        assertThat(opplastetVedlegg.filnavn).startsWith("filnavn").endsWith(".jpg")
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
        private const val TYPE = "hei|på deg"
        private const val SOKNAD_ID = 1234L
    }
}
