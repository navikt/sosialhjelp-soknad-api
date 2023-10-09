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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InformasjonRessursTest {

    private val personService: PersonService = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()

    private val ressurs = InformasjonRessurs(
        adresseSokService = mockk(),
        personService = personService,
        soknadMetadataRepository = soknadMetadataRepository,
        pabegynteSoknaderService = mockk(),
    )

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
