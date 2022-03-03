package no.nav.sosialhjelp.soknad.innsending

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import no.finn.unleash.Unleash
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService.Companion.dateTimeFormatter
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.ServiceUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneErIkkeAktivertException
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.common.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadMetadata
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
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
    private val nedetidService: NedetidService = mockk()
    private val serviceUtils: ServiceUtils = mockk()

    private val actions = SoknadActions(
        soknadService,
        kommuneInfoService,
        tilgangskontroll,
        soknadUnderArbeidRepository,
        soknadMetadataRepository,
        digisosApiService,
        unleash,
        nedetidService,
        serviceUtils
    )

    var context: ServletContext = mockk()

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { context.getRealPath(any()) } returns ""
        EIER = SubjectHandlerUtils.getUserIdFromToken()
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { unleash.isEnabled(any(), any<Boolean>()) } returns true
        every { nedetidService.isInnenforNedetid } returns false
        every { serviceUtils.isAlltidSendTilNavTestkommune() } returns false
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun sendSoknadINedetidSkalKasteException() {
        every { nedetidService.isInnenforNedetid } returns true
        every { nedetidService.nedetidSluttAsString } returns LocalDateTime.now().plusDays(2).format(dateTimeFormatter)

        assertThatExceptionOfType(SoknadenHarNedetidException::class.java)
            .isThrownBy { actions.sendSoknad("behandlingsId", context, "") }

        verify { soknadService wasNot called }
        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadMedSendingTilFiksDisabledSkalKalleSoknadService() {
        val behandlingsId = "SendingTilFiksDisabled"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadService.sendSoknad(any()) } just runs
        every { serviceUtils.isSendingTilFiksEnabled() } returns false

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
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadMetadataRepository.hent(soknadBehandlingsId) } returns soknadMetadata
        every { soknadService.sendSoknad(behandlingsId) } just runs
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

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
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadMetadataRepository.hent(soknadBehandlingsId) } returns null
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

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
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { soknadMetadataRepository.hent(soknadBehandlingsId) } returns soknadMetadata
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }
    }

    @Test
    fun sendSoknadMedFiksNedetidOgTomCacheSkalKasteException() {
        val behandlingsId = "fiksNedetidOgTomCache"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns FIKS_NEDETID_OG_TOM_CACHE
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }

        verify { soknadService wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneUtenKonfigurasjonSkalKalleSoknadService() {
        val behandlingsId = "kommuneUtenKonfigurasjon"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns MANGLER_KONFIGURASJON
        every { soknadService.sendSoknad(any()) } just runs
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { soknadService.sendSoknad(behandlingsId) }
    }

    @Test
    fun sendSoknadTilKommuneMedSvarUtSkalKalleSoknadService() {
        val behandlingsId = "kommuneMedSvarUt"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
        every { soknadService.sendSoknad(any()) } just runs
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { soknadService.sendSoknad(behandlingsId) }
    }

    @Test
    fun sendSoknadTilKommuneMedDigisosApiSkalKalleDigisosApiService() {
        val behandlingsId = "kommuneMedFDA"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        every { digisosApiService.sendSoknad(any(), any(), any()) } returns "id"
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        actions.sendSoknad(behandlingsId, context, "")

        verify(exactly = 1) { digisosApiService.sendSoknad(soknadUnderArbeid, any(), any()) }
    }

    @Test
    fun sendSoknadTilKommuneMedMidlertidigFeilSkalKasteException() {
        val behandlingsId = "kommuneMedMidlertidigFeil"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneSomIkkeErAktivertEllerSvarUtSkalKasteException() {
        val behandlingsId = "kommueMedMottakDeaktivertOgIkkeSvarut"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.mottaker.kommunenummer = "9999_kommune_uten_svarut"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER!!) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.kommuneInfo(any()) } returns HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
        every { serviceUtils.isSendingTilFiksEnabled() } returns true

        assertThatExceptionOfType(SendingTilKommuneErIkkeAktivertException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, context, "") }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun kommunenummerOrMockMedMockEnableSkalReturnereMock() {
        every { serviceUtils.isAlltidSendTilNavTestkommune() } returns true
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
