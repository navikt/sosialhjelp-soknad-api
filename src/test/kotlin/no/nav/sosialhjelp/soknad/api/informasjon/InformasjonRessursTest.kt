package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InformasjonRessursTest {

    private val personService: PersonService = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val pabegynteSoknaderService: PabegynteSoknaderService = mockk()

    companion object {
        val PERSON = Person(
            fornavn = "Test",
            mellomnavn = null,
            etternavn = "Testesen",
            fnr = "12345678910",
            sivilstatus = null,
            statsborgerskap = null,
            ektefelle = null,
            bostedsadresse = null,
            oppholdsadresse = null,
        )
    }

    private val ressurs = InformasjonRessurs(
        adresseSokService = mockk(),
        personService = personService,
        soknadMetadataRepository = soknadMetadataRepository,
        pabegynteSoknaderService = pabegynteSoknaderService,
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { personService.hentPerson(any()) } returns PERSON
        every { pabegynteSoknaderService.hentPabegynteSoknaderForBruker(any()) } returns emptyList()
        every { personService.harAdressebeskyttelse(any()) } returns false
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns emptyList()
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun `viser adressebeskyttelse`() {
        every { personService.harAdressebeskyttelse(any()) } returns false
        assertEquals(false, ressurs.getSessionInfo().userBlocked)
        every { personService.harAdressebeskyttelse(any()) } returns true
        assertEquals(true, ressurs.getSessionInfo().userBlocked)
    }

    @Test
    fun `gjengir fornavn riktig`() {
        assertEquals(PERSON.fornavn, ressurs.getSessionInfo().fornavn)
    }

    @Test
    fun `gjengir antall dager før sletting`() {
        assertEquals(14, ressurs.getSessionInfo().daysBeforeDeletion)
    }

    @Test
    fun `gjengir åpne søknader når tom liste`() {
        every { pabegynteSoknaderService.hentPabegynteSoknaderForBruker(any()) } returns emptyList()
        assertEquals(0, ressurs.getSessionInfo().open.size)
    }

    @Test
    fun `gjengir åpne søknader`() {
        every { pabegynteSoknaderService.hentPabegynteSoknaderForBruker(any()) } returns listOf(mockk(), mockk())
        assertEquals(2, ressurs.getSessionInfo().open.size)
    }

    @Test
    fun harNyligInnsendteSoknader_tomResponse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns emptyList()

        assertEquals(0, ressurs.getSessionInfo().numRecentlySent)
    }

    @Test
    fun harNyligInnsendteSoknader_flereSoknaderResponse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns listOf(mockk(), mockk())

        assertEquals(2, ressurs.getSessionInfo().numRecentlySent)
    }
}
