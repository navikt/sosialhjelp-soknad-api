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
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
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
import java.time.LocalDateTime
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
                            .withType(OpplastetVedleggType(TYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(TYPE).tilleggsinfo)
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
        val jsonVedlegg = soknadUnderArbeid.jsonInternalSoknad!!.vedlegg.vedlegg[0]
        assertThat(jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo).isEqualTo(TYPE)
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
                            .withType(OpplastetVedleggType(TYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(TYPE).tilleggsinfo)
                            .withFiler(mutableListOf(JsonFiler().withFilnavn(FILNAVN2).withSha512(SHA512)))
                            .withStatus("LastetOpp")
                    )
                )
            )
        )
        every { opplastetVedleggRepository.hentVedlegg(any(), any()) } returns OpplastetVedlegg(
            eier = "eier",
            vedleggType = OpplastetVedleggType(TYPE),
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
        assertThat(jsonVedlegg.type + "|" + jsonVedlegg.tilleggsinfo).isEqualTo(TYPE)
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
                            .withType(OpplastetVedleggType(TYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(TYPE).tilleggsinfo)
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
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(TYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(TYPE).tilleggsinfo)
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
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(TYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(TYPE).tilleggsinfo)
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
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(OpplastetVedleggType(TYPE).type)
                            .withTilleggsinfo(OpplastetVedleggType(TYPE).tilleggsinfo)
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
