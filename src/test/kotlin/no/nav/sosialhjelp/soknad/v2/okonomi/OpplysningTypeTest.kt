
package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpplysningTypeTest {
    @Test
    fun `Konvertering skal gi vare OpplysningType og InntektType`() {
        val typeString = OpplysningTypeToStringConverter.convert(InntektType.UTBETALING_HUSBANKEN)

        StringToOpplysningTypeConverter.convert(typeString).let {
            assertThat(it).isInstanceOf(OpplysningType::class.java)
            assertThat(it).isInstanceOf(InntektType::class.java)
        }
    }

    @Test
    fun `Konvertering skal gi vare OpplysningType og UtgiftType`() {
        val typeString = OpplysningTypeToStringConverter.convert(UtgiftType.UTGIFTER_STROM)

        StringToOpplysningTypeConverter.convert(typeString).let {
            assertThat(it).isInstanceOf(OpplysningType::class.java)
            assertThat(it).isInstanceOf(UtgiftType::class.java)
        }
    }

    @Test
    fun `Konvertering skal gi vare OpplysningType og FormueType`() {
        val typeString = OpplysningTypeToStringConverter.convert(FormueType.FORMUE_BRUKSKONTO)

        StringToOpplysningTypeConverter.convert(typeString).let {
            assertThat(it).isInstanceOf(OpplysningType::class.java)
            assertThat(it).isInstanceOf(FormueType::class.java)
        }
    }
}
