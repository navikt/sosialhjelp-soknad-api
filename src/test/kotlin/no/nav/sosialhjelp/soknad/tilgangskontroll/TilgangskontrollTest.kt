package no.nav.sosialhjelp.soknad.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadAlleredeSendtException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class TilgangskontrollTest {

    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val personService: PersonService = mockk()

    private val tilgangskontroll = Tilgangskontroll(soknadMetadataRepository, soknadUnderArbeidRepository, personService)

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
        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "behandlingsId",
            tilknyttetBehandlingsId = null,
            eier = userId,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(userId),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every { soknadMetadataRepository.hent(any())?.status } returns SoknadMetadataInnsendingStatus.UNDER_ARBEID
        every { soknadUnderArbeidRepository.hentSoknadNullable(any(), any()) } returns soknadUnderArbeid
        every { personService.harAdressebeskyttelse(userId) } returns false

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun `verifiserBrukerHarTilgangTilSoknad skal feile hvis SoknadUnderArbeid tilhorer andre enn innlogget bruker`() {
        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "behandlingsId",
            tilknyttetBehandlingsId = null,
            eier = "other_user",
            jsonInternalSoknad = createEmptyJsonInternalSoknad("other_user"),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        every { soknadMetadataRepository.hent(any())?.status } returns SoknadMetadataInnsendingStatus.UNDER_ARBEID
        every { soknadUnderArbeidRepository.hentSoknadNullable(any(), any()) } returns soknadUnderArbeid

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX") }
    }

    @Test
    fun `verifiserBrukerHarTilgangTilSoknad skal feile hvis soknad ikke er innsendt men soknadUnderArbeid ikke finnes`() {
        every { soknadMetadataRepository.hent(any())?.status } returns SoknadMetadataInnsendingStatus.UNDER_ARBEID
        every { soknadUnderArbeidRepository.hentSoknadNullable(any(), any()) } returns null

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun `skal kaste SoknadAlleredeSendtException hvis soknad allerede er innsendt`() {
        every { soknadMetadataRepository.hent(any())?.status } returns SoknadMetadataInnsendingStatus.FERDIG
        assertThatExceptionOfType(SoknadAlleredeSendtException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }

        every { soknadMetadataRepository.hent(any())?.status } returns SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
        assertThatExceptionOfType(SoknadAlleredeSendtException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun `bruker har tilgang til soknadens metadata hvis bruker ikke har adressebeskyttelse`() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        val metadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "123",
            fnr = userId,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
        every { soknadMetadataRepository.hent("123") } returns metadata
        every { personService.harAdressebeskyttelse(userId) } returns false

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123") }
    }

    @Test
    fun `verifiserBrukerHarTilgangTilMetadata skal feile hvis metadata tilhorer andre enn innlogget bruker`() {
        val metadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "123",
            fnr = "other_user",
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
        every { soknadMetadataRepository.hent("123") } returns metadata

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123") }
    }

    @Test
    fun `verifiserAtBrukerHarTilgang skal feile hvis bruker har adressebeskyttelse`() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        every { personService.harAdressebeskyttelse(userId) } returns true
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserAtBrukerHarTilgang() }
    }
}
