package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.mockk.every
import io.mockk.mockk
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KontonummerServiceTest {

    private val kontonummerClient: KontonummerClient = mockk()
    private val unleash: Unleash = mockk()
    private val kontonummerService = KontonummerService(kontonummerClient, unleash)

    @Test
    internal fun clientReturnererKontonummer() {
        every { unleash.isEnabled(KontonummerService.BRUK_KONTOREGISTER_ENABLED, true) } returns true
        every { kontonummerClient.getKontonummer(any()) } returns KontoDto("1337", null)

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isEqualTo("1337")
    }

//    @Test
//    internal fun clientReturnererKontonummerNull() {
//        every { kontonummerClient.getKontonummer(any()) } returns KontoDto(null, null)
//
//        val kontonummer = kontonummerService.getKontonummer("ident")
//
//        assertThat(kontonummer).isNull()
//    }

    @Test
    internal fun clientReturnererNull() {
        every { unleash.isEnabled(KontonummerService.BRUK_KONTOREGISTER_ENABLED, true) } returns true
        every { kontonummerClient.getKontonummer(any()) } returns null

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isNull()
    }
}
