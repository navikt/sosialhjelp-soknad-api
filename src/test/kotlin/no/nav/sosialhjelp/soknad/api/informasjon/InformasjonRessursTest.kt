package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class InformasjonRessursTest {

    private val messageSource: NavMessageSource = mockk()
    private val personService: PersonService = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()

    private val ressurs = InformasjonRessurs(
        messageSource,
        mockk(),
        personService,
        soknadMetadataRepository,
        mockk()
    )

    private var norskBokmaal = Locale("nb", "NO")

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun spraakDefaulterTilNorskBokmaalHvisIkkeSatt() {
        every { messageSource.getBundleFor(any(), any()) } returns mockk()

        ressurs.hentTekster(SOKNADSTYPE, null)
        ressurs.hentTekster(SOKNADSTYPE, " ")

        verify(exactly = 2) { messageSource.getBundleFor(SOKNADSTYPE, norskBokmaal) }
    }

    @Test
    fun skalHenteTeksterForSoknadsosialhjelpViaBundle() {
        every { messageSource.getBundleFor(any(), any()) } returns mockk()

        ressurs.hentTekster("soknadsosialhjelp", null)

        verify { messageSource.getBundleFor("soknadsosialhjelp", norskBokmaal) }
    }

    @Test
    fun skalHenteTeksterForAlleBundlesUtenType() {
        every { messageSource.getBundleFor(any(), any()) } returns mockk()

        ressurs.hentTekster("", null)

        verify { messageSource.getBundleFor("", norskBokmaal) }
    }

    @Test
    fun kastExceptionHvisIkkeSpraakErPaaRiktigFormat() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { ressurs.hentTekster(SOKNADSTYPE, "NORSK") }
    }

    @Test
    fun harNyligInnsendteSoknader_tomResponse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns emptyList()

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isZero
    }

    @Test
    fun harNyligInnsendteSoknader_flereSoknaderResponse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns listOf(mockk(), mockk())

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isEqualTo(2)
    }

    companion object {
        const val SOKNADSTYPE = "type"
    }
}
