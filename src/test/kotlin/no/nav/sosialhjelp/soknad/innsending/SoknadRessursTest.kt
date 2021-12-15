package no.nav.sosialhjelp.soknad.innsending

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.mappers.OkonomiMapper
import no.nav.sosialhjelp.soknad.business.service.HenvendelseService
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.common.systemdata.SystemdataUpdater
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.innsending.dto.BekreftelseRessurs
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.util.Lists
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

internal class SoknadRessursTest {

    private val soknadService: SoknadService = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val systemdata: SystemdataUpdater = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val henvendelseService: HenvendelseService = mockk()

    private val ressurs = SoknadRessurs(soknadService, soknadUnderArbeidService, soknadUnderArbeidRepository, systemdata, tilgangskontroll, henvendelseService)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())

        clearAllMocks()
        every { henvendelseService.oppdaterSistEndretDatoPaaMetadata(any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    fun skalSetteXsrfToken() {
        every { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(BEHANDLINGSID) } just runs
        val response: HttpServletResponse = mockk()
        val cookieSlot = slot<Cookie>()
        every { response.addCookie(capture(cookieSlot)) } just runs

        ressurs.hentXsrfCookie(BEHANDLINGSID, response)

        assertThat(cookieSlot.captured.name).isEqualTo(SoknadRessurs.XSRF_TOKEN + "-123")
    }

    @Test
    fun opprettingAvSoknadSkalSetteXsrfToken() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val response: HttpServletResponse = mockk()
        val cookieSlot = slot<Cookie>()
        every { response.addCookie(capture(cookieSlot)) } just runs
        every { soknadService.startSoknad(any()) } returns "null"

        ressurs.opprettSoknad(null, response, "")

        assertThat(cookieSlot.captured.name).isEqualTo(SoknadRessurs.XSRF_TOKEN + "-null")
    }

    @Test
    fun opprettSoknadUtenBehandlingsidSkalStarteNySoknad() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val response: HttpServletResponse = mockk()
        every { response.addCookie(any()) } just runs
        every { soknadService.startSoknad(any()) } returns "null"

        ressurs.opprettSoknad(null, response, "")

        verify(exactly = 1) { soknadService.startSoknad("") }
    }

    @Test
    fun opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val response: HttpServletResponse = mockk()
        every { response.addCookie(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(any(), any())
        } returns Optional.empty()
        every { soknadService.startEttersending(any()) } returns "ettersendtId"

        ressurs.opprettSoknad(BEHANDLINGSID, response, "")

        verify(exactly = 1) { soknadService.startEttersending(BEHANDLINGSID) }
    }

    @Test
    fun opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val response: HttpServletResponse = mockk()
        every { response.addCookie(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentEttersendingMedTilknyttetBehandlingsId(BEHANDLINGSID, any())
        } returns Optional.of(
            SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
                .withBehandlingsId(BEHANDLINGSID)
        )

        ressurs.opprettSoknad(BEHANDLINGSID, response, "")

        verify(exactly = 0) { soknadService.startEttersending(BEHANDLINGSID) }
    }

    @Test
    fun oppdaterSamtykkerMedTomListaSkalIkkeForeTilNoenSamtykker() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadService.oppdaterSamtykker(any(), any(), any(), any()) } just runs

        val samtykkeListe = Lists.emptyList<BekreftelseRessurs>()
        val token = "token"
        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token)

        verify(exactly = 1) { soknadService.oppdaterSamtykker(BEHANDLINGSID, false, false, token) }
    }

    @Test
    fun oppdaterSamtykkerSkalGiSamtykkerFraLista() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadService.oppdaterSamtykker(any(), any(), any(), any()) } just runs

        val bekreftelse1 = BekreftelseRessurs(BOSTOTTE_SAMTYKKE, true)
        val bekreftelse2 = BekreftelseRessurs(UTBETALING_SKATTEETATEN_SAMTYKKE, true)
        val samtykkeListe: List<BekreftelseRessurs> = Lists.newArrayList(bekreftelse1, bekreftelse2)
        val token = "token"
        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token)

        verify(exactly = 1) { soknadService.oppdaterSamtykker(BEHANDLINGSID, true, true, token) }
    }

    @Test
    fun oppdaterSamtykkerSkalGiSamtykkerFraLista_menKunDersomVerdiErSann() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadService.oppdaterSamtykker(any(), any(), any(), any()) } just runs

        val bekreftelse1 = BekreftelseRessurs(BOSTOTTE_SAMTYKKE, true)
        val bekreftelse2 = BekreftelseRessurs(UTBETALING_SKATTEETATEN_SAMTYKKE, false)
        val samtykkeListe: List<BekreftelseRessurs> = Lists.newArrayList(bekreftelse1, bekreftelse2)
        val token = "token"
        ressurs.oppdaterSamtykker(BEHANDLINGSID, samtykkeListe, token)

        verify(exactly = 1) { soknadService.oppdaterSamtykker(BEHANDLINGSID, true, false, token) }
    }

    @Test
    fun hentSamtykker_skalReturnereTomListeNarViIkkeHarNoenSamtykker() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadService.oppdaterSamtykker(any(), any(), any(), any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val token = "token"
        val bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token)

        assertThat(bekreftelseRessurser).isEmpty()
    }

    @Test
    fun hentSamtykker_skalReturnereListeMedSamtykker() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadService.oppdaterSamtykker(any(), any(), any(), any()) } just runs
        val internalSoknad = createEmptyJsonInternalSoknad(EIER)
        val opplysninger = internalSoknad.soknad.data.okonomi.opplysninger
        OkonomiMapper.setBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE, true, "Samtykke test tekst!")
        OkonomiMapper.setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, true, "Samtykke test tekst!")
        every { soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(internalSoknad)

        val token = "token"
        val bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token)

        assertThat(bekreftelseRessurser).hasSize(2)
        val bekreftelse1 = bekreftelseRessurser[0]
        assertThat(bekreftelse1.type).isEqualTo(BOSTOTTE_SAMTYKKE)
        assertThat(bekreftelse1.verdi).isTrue
        val bekreftelse2 = bekreftelseRessurser[1]
        assertThat(bekreftelse2.type).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(bekreftelse2.verdi).isTrue
    }

    @Test
    fun hentSamtykker_skalReturnereListeMedSamtykker_tarBortDeUtenSattVerdi() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadService.oppdaterSamtykker(any(), any(), any(), any()) } just runs
        val internalSoknad = createEmptyJsonInternalSoknad(EIER)
        val opplysninger = internalSoknad.soknad.data.okonomi.opplysninger
        OkonomiMapper.setBekreftelse(opplysninger, BOSTOTTE_SAMTYKKE, false, "Samtykke test tekst!")
        OkonomiMapper.setBekreftelse(opplysninger, UTBETALING_SKATTEETATEN_SAMTYKKE, true, "Samtykke test tekst!")
        every { soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(internalSoknad)

        val token = "token"
        val bekreftelseRessurser = ressurs.hentSamtykker(BEHANDLINGSID, token)
        assertThat(bekreftelseRessurser).hasSize(1)
        val bekreftelse1 = bekreftelseRessurser[0]
        assertThat(bekreftelse1.type).isEqualTo(UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(bekreftelse1.verdi).isTrue
    }

    @Test
    fun xsrfCookieSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(BEHANDLINGSID) } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { ressurs.hentXsrfCookie(BEHANDLINGSID, mockk()) }

        verify { henvendelseService wasNot called }
    }

    @Test
    fun erSystemdataEndretSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { ressurs.sjekkOmSystemdataErEndret(BEHANDLINGSID, "token") }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun oppdaterSamtykkerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { ressurs.oppdaterSamtykker(BEHANDLINGSID, emptyList(), "token") }

        verify { soknadService wasNot called }
    }

    @Test
    fun samtykkerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { ressurs.hentSamtykker(BEHANDLINGSID, "token") }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun opprettSoknadSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { ressurs.opprettSoknad(BEHANDLINGSID, mockk(), "token") }

        verify { soknadService wasNot called }
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "Hans og Grete"
    }
}
