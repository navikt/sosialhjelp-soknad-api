package no.nav.sosialhjelp.soknad.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadata
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
    private val serviceUtils: ServiceUtils = mockk()

    private val tilgangskontroll = Tilgangskontroll(soknadMetadataRepository, soknadUnderArbeidRepository, personService, serviceUtils)

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { serviceUtils.isMockAltProfil() } returns false
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun skalGiTilgangForBruker() {
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

        every { soknadUnderArbeidRepository.hentSoknadNullable(any(), any()) } returns soknadUnderArbeid
        every { personService.hentAdressebeskyttelse(userId) } returns Gradering.UGRADERT

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun skalFeileForAndre() {
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

        every { soknadUnderArbeidRepository.hentSoknadNullable(any(), any()) } returns soknadUnderArbeid

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX") }
    }

    @Test
    fun skalFeileOmSoknadenIkkeFinnes() {
        every { soknadUnderArbeidRepository.hentSoknadNullable(any(), any()) } returns null

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun skalGiTilgangForBrukerMetadata() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        val metadata = SoknadMetadata(
            id = 0L,
            behandlingsId = "123",
            fnr = userId,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
        every { soknadMetadataRepository.hent("123") } returns metadata
        every { personService.hentAdressebeskyttelse(userId) } returns Gradering.UGRADERT

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123") }
    }

    @Test
    fun skalFeileForAndreMetadata() {
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
    fun skalFeileHvisBrukerHarAdressebeskyttelseStrengtFortrolig() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        every { personService.hentAdressebeskyttelse(userId) } returns Gradering.STRENGT_FORTROLIG
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserAtBrukerHarTilgang() }
    }

    @Test
    fun skalFeileHvisBrukerHarAdressebeskyttelseFortrolig() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        every { personService.hentAdressebeskyttelse(userId) } returns Gradering.FORTROLIG
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserAtBrukerHarTilgang() }
    }
}
