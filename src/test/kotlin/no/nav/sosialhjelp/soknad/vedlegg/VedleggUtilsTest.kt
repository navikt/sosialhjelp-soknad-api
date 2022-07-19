package no.nav.sosialhjelp.soknad.vedlegg

import no.nav.sosialhjelp.soknad.vedlegg.VedleggUtils.lagFilnavn
import no.nav.sosialhjelp.soknad.vedlegg.filedetection.TikaFileType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class VedleggUtilsTest {

    @Test
    fun lagerFilnavn() {
        val filnavn = lagFilnavn("minfil.jpg", TikaFileType.JPEG, "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99")
        assertThat(filnavn).isEqualTo("minfil-5c2a1cea.jpg")

        val truncate = lagFilnavn(
            "etkjempelangtfilnavn12345678901234567890123456789012345678901234567890.jpg",
            TikaFileType.JPEG,
            "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99"
        )
        assertThat(truncate).isEqualTo("etkjempelangtfilnavn123456789012345678901234567890-5c2a1cea.jpg")

        val medSpesialTegn = lagFilnavn("en.filmedææå()ogmyerartsjø.jpg", TikaFileType.JPEG, "abc-ef05")
        assertThat(medSpesialTegn).isEqualTo("enfilmedeeaogmyerartsjo-abc.jpg")

        val utenExtension = lagFilnavn("minfil", TikaFileType.PNG, "abc-ef05")
        assertThat(utenExtension).isEqualTo("minfil-abc.png")

        val forskjelligExtension = lagFilnavn("minfil.jpg", TikaFileType.PNG, "abc-ef05")
        assertThat(forskjelligExtension).isEqualTo("minfil-abc.png")

        val caseInsensitiveExtension = lagFilnavn("minfil.JPG", TikaFileType.JPEG, "abc-ef05")
        assertThat(caseInsensitiveExtension).isEqualTo("minfil-abc.JPG")
    }
}
