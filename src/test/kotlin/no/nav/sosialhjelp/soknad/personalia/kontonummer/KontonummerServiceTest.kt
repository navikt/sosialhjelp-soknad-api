package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontonummerDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KontonummerServiceTest {

    private val kontonummerClient: KontonummerClient = mockk()
    private val kontonummerService = KontonummerService(kontonummerClient)

    @Test
    internal fun clientReturnererKontonummer() {
        every { kontonummerClient.getKontonummer(any()) } returns KontonummerDto("1337")

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isEqualTo("1337")
    }

    @Test
    internal fun clientReturnererKontonummerNull() {
        every { kontonummerClient.getKontonummer(any()) } returns KontonummerDto(null)

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isNull()
    }

    @Test
    internal fun clientReturnererNull() {
        every { kontonummerClient.getKontonummer(any()) } returns null

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isNull()
    }
}
