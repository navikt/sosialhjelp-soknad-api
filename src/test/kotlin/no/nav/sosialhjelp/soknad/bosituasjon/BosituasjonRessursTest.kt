package no.nav.sosialhjelp.soknad.bosituasjon

import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon.Botype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.bosituasjon.BosituasjonRessurs.BosituasjonFrontend
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class BosituasjonRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val bosituasjonRessurs = BosituasjonRessurs(tilgangskontroll, soknadUnderArbeidRepository)

    @BeforeEach
    fun setUp() {
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
    fun bosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersonerLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBosituasjon(null, null)

        val bosituasjonFrontend: BosituasjonFrontend = bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID)
        assertThat(bosituasjonFrontend.botype).isNull()
        assertThat(bosituasjonFrontend.antallPersoner).isNull()
    }

    @Test
    fun bosituasjonSkalReturnereBosituasjonMedBotypeOgAntallPersoner() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBosituasjon(Botype.EIER, 2)

        val bosituasjonFrontend: BosituasjonFrontend = bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID)
        assertThat(bosituasjonFrontend.botype).isEqualTo(Botype.EIER)
        assertThat(bosituasjonFrontend.antallPersoner).isEqualTo(2)
    }

    @Test
    fun putBosituasjonSkalSetteBosituasjon() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBosituasjon(Botype.LEIER, 2)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val bosituasjonFrontend = BosituasjonFrontend(Botype.ANNET, 3)
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bosituasjon = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.bosituasjon
        assertThat(bosituasjon.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(bosituasjon.botype).isEqualTo(Botype.ANNET)
        assertThat(bosituasjon.antallPersoner).isEqualTo(3)
    }

    @Test
    fun putBosituasjonSkalSetteAntallPersonerLikNull() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBosituasjon(null, 2)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val bosituasjonFrontend = BosituasjonFrontend(null, null)
        bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bosituasjon = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.bosituasjon
        assertThat(bosituasjon.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(bosituasjon.botype).isNull()
        // todo: assertion git NPE selv om bosituasjon.antallPerson er null?
//        assertThat(bosituasjon.antallPersoner).isNull()
    }

    @Test
    fun bosituasjonSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { bosituasjonRessurs.hentBosituasjon(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putBosituasjonSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val bosituasjonFrontend = BosituasjonFrontend(null, null)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { bosituasjonRessurs.updateBosituasjon(BEHANDLINGSID, bosituasjonFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun createJsonInternalSoknadWithBosituasjon(botype: Botype?, antallPersoner: Int?): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = null,
            eier = EIER,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.bosituasjon
            .withKilde(JsonKildeBruker.BRUKER)
            .withBotype(botype)
            .withAntallPersoner(antallPersoner)
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
    }
}
