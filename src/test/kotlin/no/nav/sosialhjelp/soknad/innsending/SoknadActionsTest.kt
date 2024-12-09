package no.nav.sosialhjelp.soknad.innsending

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService.Companion.dateTimeFormatter
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SoknadenHarNedetidException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.HAR_KONFIGURASJON_MED_MANGLER
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.MANGLER_KONFIGURASJON
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_SENDE_SOKNADER_VIA_FDA
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime

internal class SoknadActionsTest {
    private val soknadServiceOld: SoknadServiceOld = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val digisosApiService: DigisosApiService = mockk()
    private val nedetidService: NedetidService = mockk()
    private val navEnhetService: NavEnhetService = mockk()

    private lateinit var eier: String

    private val actions =
        SoknadActions(
            kommuneInfoService,
            tilgangskontroll,
            soknadUnderArbeidRepository,
            digisosApiService,
            nedetidService,
            navEnhetService,
        )

    private val token = "token"

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        eier = SubjectHandlerUtils.getUserIdFromToken()
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { nedetidService.isInnenforNedetid } returns false
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns createNavEnhetFrontend()
        every { soknadUnderArbeidRepository.hentSoknad(any(String::class), any()) } returns createSoknadUnderArbeid(eier)
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
        val soknadUnderArbeid = createSoknadUnderArbeid(eier)
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns FIKS_NEDETID_OG_TOM_CACHE

        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, token) }

        verify { soknadServiceOld wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneUtenKonfigurasjonSkalKalleSoknadService() {
        val behandlingsId = "kommuneUtenKonfigurasjon"
        val soknadUnderArbeid = createSoknadUnderArbeid(eier)
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns MANGLER_KONFIGURASJON

        assertThatThrownBy { actions.sendSoknad(behandlingsId, token) }
            .isInstanceOf(SendingTilKommuneUtilgjengeligException::class.java)
    }

    @Test
    fun sendSoknadTilKommuneMedSvarUtSkalKalleSoknadService() {
        val behandlingsId = "kommuneMedSvarUt"
        val soknadUnderArbeid = createSoknadUnderArbeid(eier)
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.mottaker.kommunenummer = KOMMUNE_I_SVARUT_LISTEN
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns HAR_KONFIGURASJON_MED_MANGLER

        assertThatThrownBy { actions.sendSoknad(behandlingsId, token) }
            .isInstanceOf(SendingTilKommuneUtilgjengeligException::class.java)
    }

    @Test
    fun sendSoknadTilKommuneMedDigisosApiSkalKalleDigisosApiService() {
        val behandlingsId = "kommuneMedFDA"
        val soknadUnderArbeid = createSoknadUnderArbeid(eier)
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns SKAL_SENDE_SOKNADER_VIA_FDA
        every { digisosApiService.sendSoknad(any(), any(), any()) } returns "id"
        every { digisosApiService.getTimestampSistSendtSoknad(any()) } returns Instant.now().toEpochMilli()

        actions.sendSoknad(behandlingsId, token)

        verify(exactly = 1) { digisosApiService.sendSoknad(soknadUnderArbeid, any(), any()) }
    }

    @Test
    fun sendSoknadTilKommuneMedMidlertidigFeilSkalKasteException() {
        val behandlingsId = "kommuneMedMidlertidigFeil"
        val soknadUnderArbeid = createSoknadUnderArbeid(eier)
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.mottaker.kommunenummer = "1234"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD

        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException::class.java)
            .isThrownBy { actions.sendSoknad(behandlingsId, token) }

        verify { digisosApiService wasNot called }
    }

    @Test
    fun sendSoknadTilKommuneSomIkkeErAktivertEllerSvarUtSkalKasteException() {
        val behandlingsId = "kommueMedMottakDeaktivertOgIkkeSvarut"
        val soknadUnderArbeid = createSoknadUnderArbeid(eier)
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.mottaker.kommunenummer = "9999_kommune_uten_svarut"
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns HAR_KONFIGURASJON_MED_MANGLER

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

    @Test
    fun `Ved manglende kommunenummer skal det utledes pa nytt`() {
        val kommunenummerSlot = slot<String>()

        val soknadUnderArbeid =
            createSoknadUnderArbeid(eier)
                .apply {
                    jsonInternalSoknad!!.soknad.data.personalia.oppholdsadresse = JsonAdresse()
                    jsonInternalSoknad!!.soknad.data.personalia.oppholdsadresse.adresseValg = JsonAdresseValg.FOLKEREGISTRERT
                }

        every { soknadUnderArbeidRepository.hentSoknad("behandlingsid", eier) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns createNavEnhetFrontend()
        every { kommuneInfoService.getKommuneStatus(any(), true) } returns SKAL_SENDE_SOKNADER_VIA_FDA
        every { digisosApiService.getTimestampSistSendtSoknad(any()) } returns Instant.now().toEpochMilli()
        every { digisosApiService.sendSoknad(any(), any(), any()) } returns "id"

        soknadUnderArbeid.jsonInternalSoknad!!.apply {
            soknad.mottaker.kommunenummer = null
        }

        actions.sendSoknad("behandlingsid", token)

        verify(exactly = 1) { digisosApiService.sendSoknad(soknadUnderArbeid, any(), capture(kommunenummerSlot)) }
        assertThat(kommunenummerSlot.captured).isEqualTo("0301")

        soknadUnderArbeid.jsonInternalSoknad
            .also {
                assertThat(it!!.mottaker.navEnhetsnavn).isEqualTo("NAV Oslo, Oslo")
                assertThat(it.soknad.mottaker.kommunenummer).isEqualTo("0301")
            }
    }

    private fun createNavEnhetFrontend(): NavEnhetFrontend {
        return NavEnhetFrontend(
            orgnr = "123456789",
            enhetsnr = "030192",
            enhetsnavn = "NAV Oslo",
            kommunenavn = "Oslo",
            kommuneNr = "0301",
            behandlingsansvarlig = "Per",
            valgt = true,
            isMottakMidlertidigDeaktivert = false,
            isMottakDeaktivert = false,
        )
    }

    companion object {
        private const val KOMMUNE_I_SVARUT_LISTEN = "0301"

        private fun createSoknadUnderArbeid(eier: String): SoknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                eier = eier,
                jsonInternalSoknad =
                    createEmptyJsonInternalSoknad(eier, false).apply {
                        soknad.data.personalia.oppholdsadresse =
                            JsonAdresse()
                                .withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT)
                        soknad.mottaker =
                            JsonSoknadsmottaker()
                                .withKommunenummer("0301")
                    },
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )
    }
}
