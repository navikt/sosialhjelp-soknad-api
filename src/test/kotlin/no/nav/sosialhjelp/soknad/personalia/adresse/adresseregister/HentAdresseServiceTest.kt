package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.app.subjecthandler.TokenUtils
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.Bydel
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkelNummer
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.util.TestSubjectHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HentAdresseServiceTest {
    private val hentAdresseClient: HentAdresseClient = mockk()
    private val hentAdresseService = HentAdresseService(hentAdresseClient)

    private val defaultMatrikkelAdresse =
        MatrikkeladresseDto(
            undernummer = "01234",
            matrikkelnummer =
                MatrikkelNummer(
                    kommunenummer = "0301",
                    gaardsnummer = "000123",
                    bruksnummer = "H0101",
                    festenummer = "F4",
                    seksjonsnummer = "seksjonsnummer",
                ),
            bydel =
                Bydel(
                    bydelsnummer = "030107",
                ),
        )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        TokenUtils.setNewSubjectHandler(TestSubjectHandler)
    }

    @AfterEach
    internal fun tearDown() {
        TokenUtils.resetSubjectHandlerImpl()
    }

    @Test
    internal fun `null gir null`() {
        every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns null
        assertThat(hentAdresseService.hentKartverketMatrikkelAdresse("matrikkeId")).isNull()
    }

    @Test
    internal fun `henter adresse fra pdl`() {
        every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns defaultMatrikkelAdresse
        val dto = hentAdresseService.hentKartverketMatrikkelAdresse("matrikkelId")
        assertThat(dto).isNotNull
        assertThat(dto?.kommunenummer).isEqualTo("0301")
    }
}
