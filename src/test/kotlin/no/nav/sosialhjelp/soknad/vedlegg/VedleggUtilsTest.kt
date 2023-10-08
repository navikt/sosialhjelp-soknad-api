package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.repository.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.EXCEL_FILE
import no.nav.sosialhjelp.soknad.util.ExampleFileRepository.PDF_FILE
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.finnVedleggEllerKastException
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.validerFil
import no.nav.sosialhjelp.soknad.vedlegg.exceptions.UgyldigOpplastingTypeException
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.FileDetectionUtils.detectMimeType
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.TikaFileType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class VedleggUtilsTest {

    @Test
    fun lagerFilnavn() {
        val uuid = UUID.fromString("5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99")
        val uuidFirstPart = uuid.toString().split("-")[0]
        val filnavn = lagFilnavn("minfil.jpg", TikaFileType.JPEG, uuid)
        assertThat(filnavn).isEqualTo("minfil-$uuidFirstPart.jpg")

        val truncate = lagFilnavn(
            "etkjempelangtfilnavn12345678901234567890123456789012345678901234567890.jpg",
            TikaFileType.JPEG,
            uuid
        )
        assertThat(truncate).isEqualTo("etkjempelangtfilnavn123456789012345678901234567890-$uuidFirstPart.jpg")

        val medSpesialTegn = lagFilnavn("en.filmedææå()ogmyerartsjø.jpg", TikaFileType.JPEG, uuid)
        assertThat(medSpesialTegn).isEqualTo("enfilmedeeaogmyerartsjo-$uuidFirstPart.jpg")

        val utenExtension = lagFilnavn("minfil", TikaFileType.PNG, uuid)
        assertThat(utenExtension).isEqualTo("minfil-$uuidFirstPart.png")

        val forskjelligExtension = lagFilnavn("minfil.jpg", TikaFileType.PNG, uuid)
        assertThat(forskjelligExtension).isEqualTo("minfil-$uuidFirstPart.png")

        val caseInsensitiveExtension = lagFilnavn("minfil.JPG", TikaFileType.JPEG, uuid)
        assertThat(caseInsensitiveExtension).isEqualTo("minfil-$uuidFirstPart.JPG")
    }

    @Test
    fun `Validering av excel-fil kaster feil`() {
        val file = EXCEL_FILE
        assertThatThrownBy { validerFil(file.readBytes(), file.name) }
            .isInstanceOf(UgyldigOpplastingTypeException::class.java)
            .hasMessageContaining("Ugyldig filtype for opplasting")
    }

    @Test
    fun `Validering av PDF-fil gir riktig TikaType`() {

        val file = PDF_FILE
        val parts = file.name.split(".")
        val navn = parts[0]
        val ext = parts[1]

        val tikaFileType = validerFil(file.readBytes(), file.name)

        val uuidFromBytes = UUID.nameUUIDFromBytes(file.readBytes())
        val first = uuidFromBytes.toString().split("-")[0]
        val filnavn = lagFilnavn(file.name, tikaFileType, uuidFromBytes)
        assertThat(filnavn).isEqualTo("$navn-$first.$ext")
    }

    @Test
    fun `FinnVedleggEllerKastException() finner vedlegg basert pa type og tilleggsinfo`() {

        val vedleggType = OpplastetVedleggType("hei|på deg")

        val soknadUnderArbeid = createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    listOf(
                        JsonVedlegg()
                            .withType(vedleggType.type)
                            .withTilleggsinfo(vedleggType.tilleggsinfo)
                            .withStatus("VedleggKreves")
                    )
                )
            )
        )
        val vedlegg = finnVedleggEllerKastException("hei|på deg", soknadUnderArbeid)
        assertThat(vedlegg.type).isEqualTo(soknadUnderArbeid.jsonInternalSoknad!!.vedlegg.vedlegg[0].type)
    }

    @Test
    fun `Kast exception hvis vedlegg ikke finnes`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(
            JsonInternalSoknad().withVedlegg(
                JsonVedleggSpesifikasjon().withVedlegg(
                    emptyList()
                )
            )
        )
        assertThatThrownBy { finnVedleggEllerKastException("hei|på deg", soknadUnderArbeid) }
            .isInstanceOf(IkkeFunnetException::class.java)
            .hasMessageContaining("Dette vedlegget tilhører hei|på deg utgift som har blitt tatt bort fra søknaden.")
    }

    @Test
    fun `Fil med samme navn, men annet innhold gir annet filnavn`() {
        val (filnavn1, data1) = VedleggUtils.behandleFilOgReturnerFildata(PDF_FILE.name, PDF_FILE.readBytes())

        val alteredBytes = PDF_FILE.readBytes()
        alteredBytes[alteredBytes.size / 2] = alteredBytes[alteredBytes.size / 2].inc()

        val (filnavn2, data2) = VedleggUtils.behandleFilOgReturnerFildata(PDF_FILE.name, alteredBytes)

        assertThat(detectMimeType(data1)).isEqualTo(detectMimeType(data2))
        assertThat(data1).isNotEqualTo(data2)
        assertThat(filnavn1).isNotEqualTo(filnavn2)
    }

    @Test
    fun `Konverterer fil hvis excel`() {

        assertThatThrownBy { validerFil(EXCEL_FILE.readBytes(), EXCEL_FILE.name) }
            .isInstanceOf(UgyldigOpplastingTypeException::class.java)

        val (filnavn, data) =
            VedleggUtils.behandleFilOgReturnerFildata(EXCEL_FILE.name, EXCEL_FILE.readBytes())

        assertThat(filnavn).contains(".pdf")
        val fileType = validerFil(data, filnavn)
        assertThat(fileType).isEqualTo(TikaFileType.PDF)
    }

    private fun createSoknadUnderArbeid(jsonInternalSoknad: JsonInternalSoknad): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "123",
            tilknyttetBehandlingsId = null,
            eier = "EIER",
            jsonInternalSoknad = jsonInternalSoknad,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }
}
