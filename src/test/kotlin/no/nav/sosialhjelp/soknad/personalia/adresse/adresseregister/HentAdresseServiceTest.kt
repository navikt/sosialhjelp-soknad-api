package no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.Bydel
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkelNummer
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HentAdresseServiceTest {
    private val hentAdresseClient: HentAdresseClient = mockk()
    private val personService: PersonService = mockk()
    private val hentAdresseService = HentAdresseService(hentAdresseClient, personService)

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
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    internal fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
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

    @Test
    internal fun `hent adresse for innlogget bruker`() {
        val mockPerson: Person = mockk()
        every { personService.hentPerson(any()) } returns mockPerson
        every { mockPerson.bostedsadresse?.matrikkeladresse?.matrikkelId } returns "matrikkelId"
        every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns defaultMatrikkelAdresse
        val dto = hentAdresseService.hentKartverketMatrikkelAdresseForInnloggetBruker()
        assertThat(dto).isNotNull
        assertThat(dto?.kommunenummer).isEqualTo("0301")
    }

    @Test
    internal fun `hent adresse for innlogget bruker - returnerer null hvis person personService gir null`() {
        every { personService.hentPerson(any()) } returns null
        val dto = hentAdresseService.hentKartverketMatrikkelAdresseForInnloggetBruker()
        assertThat(dto).isNull()
    }

    @Test
    internal fun `hent adresse for innlogget bruker - kast feil hvis person personService feiler`() {
        every { personService.hentPerson(any()) } throws PdlApiException("pdl feil")
        assertThatExceptionOfType(PdlApiException::class.java).isThrownBy {
            hentAdresseService.hentKartverketMatrikkelAdresseForInnloggetBruker()
        }
    }
}
