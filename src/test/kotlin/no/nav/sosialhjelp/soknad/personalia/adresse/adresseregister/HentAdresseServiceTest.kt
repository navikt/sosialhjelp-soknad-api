package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkelNummer
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class HentAdresseServiceTest {

    private val hentAdresseClient: HentAdresseClient = mockk()
    private val hentAdresseService = HentAdresseService(hentAdresseClient)

    @Test
    internal fun `null gir null`() {
        every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns null
        assertThat(hentAdresseService.hentKartverketMatrikkelAdresse("matrikkeId")).isNull()
    }

    @Test
    internal fun `henter adresse fra pdl`() {
        every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns createMatrikkeladresseDto()
        val dto = hentAdresseService.hentKartverketMatrikkelAdresse("matrikkelId")
        assertThat(dto).isNotNull
        assertThat(dto?.kommunenummer).isEqualTo("0301")
    }

    private fun createMatrikkeladresseDto(): MatrikkeladresseDto {
        return MatrikkeladresseDto(
            undernummer = "01234",
            matrikkelnummer = MatrikkelNummer(
                kommunenummer = "0301",
                gaardsnummer = "000123",
                bruksnummer = "H0101",
                festenummer = "F4",
                seksjonsnummer = "seksjonsnummer"
            )
        )
    }
}
