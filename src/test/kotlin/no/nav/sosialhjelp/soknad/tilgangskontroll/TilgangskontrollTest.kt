package no.nav.sosialhjelp.soknad.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

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
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withEier(userId)
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(userId))
        every { soknadUnderArbeidRepository.hentSoknadOptional(any(), any()) } returns Optional.of(soknadUnderArbeid)
        every { personService.hentAdressebeskyttelse(userId) } returns Gradering.UGRADERT

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun skalFeileForAndre() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("other_user"))
        every { soknadUnderArbeidRepository.hentSoknadOptional(any(), any()) } returns Optional.of(soknadUnderArbeid)

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX") }
    }

    @Test
    fun skalFeileOmSoknadenIkkeFinnes() {
        every { soknadUnderArbeidRepository.hentSoknadOptional(any(), any()) } returns Optional.empty()

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123") }
    }

    @Test
    fun skalGiTilgangForBrukerMetadata() {
        val userId = SubjectHandlerUtils.getUserIdFromToken()
        val metadata = SoknadMetadata()
        metadata.fnr = userId
        every { soknadMetadataRepository.hent("123") } returns metadata
        every { personService.hentAdressebeskyttelse(userId) } returns Gradering.UGRADERT

        assertThatNoException()
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123") }
    }

    @Test
    fun skalFeileForAndreMetadata() {
        val metadata = SoknadMetadata()
        metadata.fnr = "other_user"
        every { soknadMetadataRepository.hent("123") } returns metadata

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilMetadata("123") }
    }

    @Test
    fun skalFeileHvisEierErNull() {
        every { soknadUnderArbeidRepository.hentSoknadOptional(any(), any()) } returns Optional.of(SoknadUnderArbeid())

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("") }
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
