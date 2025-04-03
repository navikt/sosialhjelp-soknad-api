package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.v2.kontakt.KortSoknadUseCaseHandler
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.unit.DataSize

internal class InformasjonRessursTest {
    private val personService: PersonService = mockk()
    private val soknadMetadataService: SoknadMetadataService = mockk()
    private val pabegynteSoknaderService: PabegynteSoknaderService = mockk()
    private val kortSoknadHandler: KortSoknadUseCaseHandler = mockk()

    companion object {
        val PERSON =
            Person(
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

    private val ressurs =
        InformasjonRessurs(
            adresseSokService = mockk(),
            personService = personService,
            pabegynteSoknaderService = pabegynteSoknaderService,
            maxUploadSize = DataSize.ofTerabytes(10),
            soknadMetadataService = soknadMetadataService,
            kortSoknadUseCaseHandler = kortSoknadHandler,
        )

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { kortSoknadHandler.isQualifiedFromFiks(any(), any()) } returns false
        every { personService.hentPerson(any(), any()) } returns PERSON
        every { pabegynteSoknaderService.hentPabegynteSoknaderForBruker(any()) } returns emptyList()
        every { personService.harAdressebeskyttelse(any()) } returns false
        every { soknadMetadataService.getNumberOfSoknaderSentAfter(any(), any()) } returns 2
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
        every { soknadMetadataService.getNumberOfSoknaderSentAfter(any(), any()) } returns 0

        assertEquals(0, ressurs.getSessionInfo().numRecentlySent)
    }

    @Test
    fun harNyligInnsendteSoknader_flereSoknaderResponse() {
        every { soknadMetadataService.getNumberOfSoknaderSentAfter(any(), any()) } returns 2

        assertEquals(2, ressurs.getSessionInfo().numRecentlySent)
    }
}
