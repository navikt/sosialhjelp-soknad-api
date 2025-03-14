package no.nav.sosialhjelp.soknad.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.metadata.Tidspunkt
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.util.UUID

internal class TilgangskontrollTest {
    private val soknadService: SoknadService = mockk(relaxed = true)
    private val personService: PersonService = mockk()
    private val environment: Environment = mockk()
    private val soknadMetadataService: SoknadMetadataService = mockk()

    private val tilgangskontroll =
        Tilgangskontroll(
            soknadService,
            personService,
            environment,
            soknadMetadataService,
        )

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { MiljoUtils.isMockAltProfil() } returns false
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun `verifiserBrukerHarTilgangTilSoknad skal gi tilgang`() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        val soknadId = UUID.randomUUID()

        every { soknadService.getSoknadOrNull(soknadId) } returns
            Soknad(soknadId, userId, kortSoknad = false)
        every { soknadMetadataService.getMetadataForSoknad(any()) } returns
            SoknadMetadata(
                soknadId = soknadId,
                personId = userId,
                status = SoknadStatus.OPPRETTET,
                tidspunkt = Tidspunkt(),
            )
        every { personService.harAdressebeskyttelse(userId) } returns false

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(soknadId.toString()) }
    }

    @Test
    fun `verifiserBrukerHarTilgangTilSoknad skal feile hvis SoknadUnderArbeid tilhorer andre enn innlogget bruker`() {
        every { soknadMetadataService.getMetadataForSoknad(any()) } returns
            SoknadMetadata(
                soknadId = UUID.randomUUID(),
                personId = "12345612345",
                status = SoknadStatus.OPPRETTET,
                tidspunkt = Tidspunkt(),
            )

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(UUID.randomUUID().toString()) }
    }

    @Test
    fun `verifiserBrukerHarTilgangTilSoknad skal feile hvis soknad ikke er innsendt men soknadUnderArbeid ikke finnes`() {
        every { soknadMetadataService.getMetadataForSoknad(any()) } throws IkkeFunnetException("ikke funnet")

        assertThatExceptionOfType(IkkeFunnetException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("54e7c9b2-0b25-4e2c-aa7c-bf8898a6b388") }
    }

    @Test
    fun `skal kaste SoknadAlleredeSendtException hvis soknad allerede er innsendt`() {
        every { soknadMetadataService.getMetadataForSoknad(any()) } returns
            SoknadMetadata(
                soknadId = UUID.randomUUID(),
                personId = "12345612345",
                status = SoknadStatus.SENDT,
                tidspunkt = Tidspunkt(sendtInn = LocalDateTime.now()),
            )
        assertThatExceptionOfType(SoknadAlleredeSendtException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(UUID.randomUUID().toString()) }
    }

    @Test
    fun `verifiserAtBrukerHarTilgang skal feile hvis bruker har adressebeskyttelse`() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        every { personService.harAdressebeskyttelse(userId) } returns true
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserAtBrukerHarTilgang() }
    }
}
