package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.UtenlandskKontoInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EierServiceTest {
    private val kontonummerClient: KontonummerClient = mockk()
    private val kontonummerService = KontonummerService(kontonummerClient)

    @Test
    internal fun clientReturnererKontonummer() {
        every { kontonummerClient.getKontonummer(any()) } returns KontoDto("1337", null)

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isEqualTo("1337")
    }

    @Test
    internal fun clientReturnererNull() {
        every { kontonummerClient.getKontonummer(any()) } returns null

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isNull()
    }

    @Test
    internal fun kontonummerSkalIkkeSettesNaarKlientReturnererUtenlandskontoNr() {
        every { kontonummerClient.getKontonummer(any()) } returns
            KontoDto(
                "1337",
                UtenlandskKontoInfo(null, null, bankLandkode = "SWE", valutakode = "SEK", null, null, null, null),
            )

        val kontonummer = kontonummerService.getKontonummer("ident")

        assertThat(kontonummer).isNull()
    }
}
