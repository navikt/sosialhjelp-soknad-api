package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.KontoDto
import no.nav.sosialhjelp.soknad.personalia.kontonummer.dto.UtenlandskKontoInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EierServiceTest {
    private val kontonummerClient: KontonummerClient = mockk()
    private val kontonummerService = KontonummerService(kontonummerClient)

    @Test
    internal suspend fun clientReturnererKontonummer() {
        coEvery { kontonummerClient.getKontonummer() } returns KontoDto("1337", null)

        val kontonummer = kontonummerService.getKontonummer()

        assertThat(kontonummer).isEqualTo("1337")
    }

    @Test
    internal suspend fun clientReturnererNull() {
        coEvery { kontonummerClient.getKontonummer() } returns null

        val kontonummer = kontonummerService.getKontonummer()

        assertThat(kontonummer).isNull()
    }

    @Test
    internal suspend fun kontonummerSkalIkkeSettesNaarKlientReturnererUtenlandskontoNr() {
        coEvery { kontonummerClient.getKontonummer() } returns
            KontoDto(
                "1337",
                UtenlandskKontoInfo(null, null, bankLandkode = "SWE", valutakode = "SEK", null, null, null, null),
            )

        val kontonummer = kontonummerService.getKontonummer()

        assertThat(kontonummer).isNull()
    }
}
