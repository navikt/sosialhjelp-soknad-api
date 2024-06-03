package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OkonomiTypeTest {
    @Test
    fun `Konvertering skal gi vare OkonomiType og InntektType`() {
        val typeString = OkonomiTypeToStringConverter.convert(InntektType.UTBETALING_HUSBANKEN)

        StringToOkonomiTypeConverter.convert(typeString).let {
            assertThat(it).isInstanceOf(OkonomiType::class.java)
            assertThat(it).isInstanceOf(InntektType::class.java)
        }
    }

    @Test
    fun `Konvertering skal gi vare OkonomiType og UtgiftType`() {
        val typeString = OkonomiTypeToStringConverter.convert(UtgiftType.UTGIFTER_STROM)

        StringToOkonomiTypeConverter.convert(typeString).let {
            assertThat(it).isInstanceOf(OkonomiType::class.java)
            assertThat(it).isInstanceOf(UtgiftType::class.java)
        }
    }

    @Test
    fun `Konvertering skal gi vare OkonomiType og FormueType`() {
        val typeString = OkonomiTypeToStringConverter.convert(FormueType.FORMUE_BRUKSKONTO)

        StringToOkonomiTypeConverter.convert(typeString).let {
            assertThat(it).isInstanceOf(OkonomiType::class.java)
            assertThat(it).isInstanceOf(FormueType::class.java)
        }
    }
}
