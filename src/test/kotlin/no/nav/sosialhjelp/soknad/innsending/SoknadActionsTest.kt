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
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService.Companion.dateTimeFormatter
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadActionsTest {

    private val soknadServiceOld: SoknadServiceOld = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val digisosApiService: DigisosApiService = mockk()
    private val nedetidService: NedetidService = mockk()

    private val actions = SoknadActions(
        soknadServiceOld,
        kommuneInfoService,
        tilgangskontroll,
        soknadUnderArbeidRepository,
        soknadMetadataRepository,
        digisosApiService,
        nedetidService
    )

    private val token = "token"

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        EIER = SubjectHandlerUtils.getUserIdFromToken()
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { nedetidService.isInnenforNedetid } returns false
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
            .isThrownBy { actions.sendSoknad("behandlingsId", token) }

        verify { soknadServiceOld wasNot called }
        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadMedFiksNedetidOgTomCacheSkalKasteException() {
        val behandlingsId = "fiksNedetidOgTomCache"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns FIKS_NEDETID_OG_TOM_CACHE

        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, token) }

        verify { soknadServiceOld wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneUtenKonfigurasjonSkalKasteException() {
        val behandlingsId = "kommuneUtenKonfigurasjon"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns MANGLER_KONFIGURASJON

        assertThatThrownBy { actions.sendSoknad(behandlingsId, token) }
            .isInstanceOf(SendingTilKommuneUtilgjengeligException::class.java)
    }

    @Test
    fun sendSoknadTilKommuneMedSvarUtSkalKasteException() {
        val behandlingsId = "kommuneMedSvarUt"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT

        assertThatThrownBy { actions.sendSoknad(behandlingsId, token) }
            .isInstanceOf(SendingTilKommuneUtilgjengeligException::class.java)
    }

    @Test
    fun sendSoknadTilKommuneMedDigisosApiSkalKalleDigisosApiService() {
        val behandlingsId = "kommuneMedFDA"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns SKAL_SENDE_SOKNADER_OG_ETTERSENDELSER_VIA_FDA
        every { digisosApiService.sendSoknad(any(), any(), any()) } returns "id"

        actions.sendSoknad(behandlingsId, token)

        verify(exactly = 1) { digisosApiService.sendSoknad(soknadUnderArbeid, any(), any()) }
    }

    @Test
    fun sendSoknadTilKommuneMedMidlertidigFeilSkalKasteException() {
        val behandlingsId = "kommuneMedMidlertidigFeil"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD_OG_ETTERSENDELSER

        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, token) }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneSomIkkeErAktivertEllerSvarUtSkalKasteException() {
        val behandlingsId = "kommueMedMottakDeaktivertOgIkkeSvarut"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker.kommunenummer = "9999_kommune_uten_svarut"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, EIER) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns HAR_KONFIGURASJON_MEN_SKAL_SENDE_VIA_SVARUT

        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, token) }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadSkalGiAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { actions.sendSoknad("behandlingsId", token) }

        verify { soknadServiceOld wasNot called }
        verify { kommuneInfoService wasNot called }
        verify { digisosApiService wasNot called }
    }

    companion object {
        private lateinit var EIER: String

        private const val KOMMUNE_I_SVARUT_LISTEN = "0301"

        private fun createSoknadUnderArbeid(eier: String): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                eier = eier,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(eier),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
