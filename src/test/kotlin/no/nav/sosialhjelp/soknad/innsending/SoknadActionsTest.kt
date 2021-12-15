package no.nav.sosialhjelp.soknad.innsending

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils.NEDETID_SLUTT
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidUtils.NEDETID_START
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErIkkeAktivertException
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.business.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.business.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import javax.servlet.ServletContext

internal class SoknadActionsTest {
    private var EIER: String? = null

    private val soknadService: SoknadService = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val digisosApiService: DigisosApiService = mockk()
    private val unleash: Unleash = mockk()

    private val actions = SoknadActions(
        soknadService,
        kommuneInfoService,
        tilgangskontroll,
        soknadUnderArbeidRepository,
        soknadMetadataRepository,
        digisosApiService,
        unleash
    )

    var context: ServletContext = mockk()

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())

        clearAllMocks()
        every { context.getRealPath(any()) } returns ""
        EIER = SubjectHandler.getUserId()
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { unleash.isEnabled(any(), any<Boolean>()) } returns true
    }

    @AfterEach
    fun tearDown() {
        System.clearProperty("digisosapi.sending.alltidTilTestkommune.enable")
        System.clearProperty("digisosapi.sending.enable")
        System.clearProperty(NEDETID_START)
        System.clearProperty(NEDETID_SLUTT)
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    fun sendSoknadINedetidSkalKasteException() {
        System.setProperty(NEDETID_START, LocalDateTime.now().minusDays(1).format(NedetidUtils.dateTimeFormatter))
        System.setProperty(NEDETID_SLUTT, LocalDateTime.now().plusDays(2).format(NedetidUtils.dateTimeFormatter))

        assertThatExceptionOfType(SoknadenHarNedetidException::class.java)
            .isThrownBy { actions.sendSoknad("behandlingsId", context, "") }

        verify { soknadService wasNot called }
        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadMedSoknadUnderArbeidNullSkalKasteException() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns null

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { actions.sendSoknad("behandlingsId", context, "") }

        verify(exactly = 0) { soknadService.sendSoknad(any()) }
        verify(exactly = 0) { digisosApiService.sendSoknad(any(), any(), any()) }
    }

    @Test
    fun sendSoknadMedSendingTilFiksDisabledSkalKalleSoknadService() {
        val behandlingsId = "SendingTilFiksDisabled"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadService.sendSoknad(any()) } just runs

        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { soknadService.sendSoknad(behandlingsId) }
    }

    @Test
    fun sendEttersendelsePaaSvarutSoknadSkalKalleSoknadService() {
        val behandlingsId = "ettersendelsePaaSvarUtSoknad"
        val soknadBehandlingsId = "soknadSendtViaSvarUt"
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
            .withTilknyttetBehandlingsId(soknadBehandlingsId)
        val soknadMetadata = SoknadMetadata()
        soknadMetadata.status = SoknadMetadataInnsendingStatus.UNDER_ARBEID
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadMetadataRepository.hent(soknadBehandlingsId) } returns soknadMetadata
        every { soknadService.sendSoknad(behandlingsId) } just runs

        System.setProperty("digisosapi.sending.enable", "true")
        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { soknadService.sendSoknad(behandlingsId) }
    }

    @Test
    fun sendEttersendelsePaaSoknadUtenMetadataSkalGiException() {
        val behandlingsId = "ettersendelsePaaSoknadUtenMetadata"
        val soknadBehandlingsId = "soknadSendtViaSvarUt"
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
            .withTilknyttetBehandlingsId(soknadBehandlingsId)
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadMetadataRepository.hent(soknadBehandlingsId) } returns null

        System.setProperty("digisosapi.sending.enable", "true")

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }
    }

    @Test
    fun sendEttersendelsePaaDigisosApiSoknadSkalGiException() {
        val behandlingsId = "ettersendelsePaaDigisosApiSoknad"
        val soknadBehandlingsId = "soknadSendtViaSvarUt"
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
            .withTilknyttetBehandlingsId(soknadBehandlingsId)
        val soknadMetadata = SoknadMetadata()
        soknadMetadata.status = SoknadMetadataInnsendingStatus.SENDT_MED_DIGISOS_API
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadMetadataRepository.hent(soknadBehandlingsId) } returns soknadMetadata

        System.setProperty("digisosapi.sending.enable", "true")
        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }
    }

    @Test
    fun sendSoknadMedFiksNedetidOgTomCacheSkalKasteException() {
        val behandlingsId = "fiksNedetidOgTomCache"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns FIKS_NEDETID_OG_TOM_CACHE

        System.setProperty("digisosapi.sending.enable", "true")
        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }

        verify { soknadService wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneUtenKonfigurasjonSkalKalleSoknadService() {
        val behandlingsId = "kommuneUtenKonfigurasjon"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns MANGLER_KONFIGURASJON
        every { soknadService.sendSoknad(any()) } just runs

        System.setProperty("digisosapi.sending.enable", "true")
        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { soknadService.sendSoknad(behandlingsId) }
    }

    @Test
    fun sendSoknadTilKommuneMedSvarUtSkalKalleSoknadService() {
        val behandlingsId = "kommuneMedSvarUt"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
        every { soknadService.sendSoknad(any()) } just runs

        System.setProperty("digisosapi.sending.enable", "true")
        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { soknadService.sendSoknad(behandlingsId) }
    }

    @Test
    fun sendSoknadTilKommuneMedDigisosApiSkalKalleDigisosApiService() {
        val behandlingsId = "kommuneMedFDA"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        every { digisosApiService.sendSoknad(any(), any(), any()) } returns "id"

        System.setProperty("digisosapi.sending.enable", "true")
        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { digisosApiService.sendSoknad(soknadUnderArbeid, any(), any()) }
    }

    @Test
    fun sendSoknadTilKommuneMedMidlertidigFeilSkalKasteException() {
        val behandlingsId = "kommuneMedMidlertidigFeil"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER

        System.setProperty("digisosapi.sending.enable", "true")
        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneSomIkkeErAktivertEllerSvarUtSkalKasteException() {
        val behandlingsId = "kommueMedMottakDeaktivertOgIkkeSvarut"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = "9999_kommune_uten_svarut"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT

        System.setProperty("digisosapi.sending.enable", "true")
        assertThatExceptionOfType(SendingTilKommuneErIkkeAktivertException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun kommunenummerOrMockMedMockEnableSkalReturnereMock() {
        System.setProperty("digisosapi.sending.alltidTilTestkommune.enable", "true")
        val kommunenummer = actions.getKommunenummerOrMock(SoknadUnderArbeid())
        assertThat(kommunenummer).isEqualTo(TESTKOMMUNE)
    }

    @Test
    fun kommunenummerOrMockUtenMockSkalIkkeReturnereMock() {
        val expectedKommunenummer = "1111"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.withKommunenummer(expectedKommunenummer)
        val kommunenummer = actions.getKommunenummerOrMock(soknadUnderArbeid)
        assertThat(kommunenummer).isEqualTo(expectedKommunenummer)
    }

    @Test
    fun sendSoknadSkalGiAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { actions.sendSoknad("behandlingsId", mockk(), "token") }

        verify { soknadService wasNot called }
        verify { kommuneInfoService wasNot called }
        verify { digisosApiService wasNot called }
    }

    companion object {
        const val TESTKOMMUNE = "3002"
        const val KOMMUNE_I_SVARUT_LISTEN = "0301"
    }
}
