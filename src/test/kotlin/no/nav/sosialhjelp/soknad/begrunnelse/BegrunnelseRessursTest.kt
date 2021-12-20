package no.nav.sosialhjelp.soknad.begrunnelse

import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.begrunnelse.BegrunnelseRessurs.BegrunnelseFrontend
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BegrunnelseRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val begrunnelseRessurs = BegrunnelseRessurs(tilgangskontroll, soknadUnderArbeidRepository)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    fun begrunnelseSkalReturnereBegrunnelseMedTommeStrenger() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithBegrunnelse("", "")
        val begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID)
        assertThat(begrunnelseFrontend.hvaSokesOm).isBlank
        assertThat(begrunnelseFrontend.hvorforSoke).isBlank
    }

    @Test
    fun begrunnelseSkalReturnereBegrunnelse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBegrunnelse(SOKER_OM, SOKER_FORDI)

        val begrunnelseFrontend = begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID)
        assertThat(begrunnelseFrontend.hvaSokesOm).isEqualTo(SOKER_OM)
        assertThat(begrunnelseFrontend.hvorforSoke).isEqualTo(SOKER_FORDI)
    }

    @Test
    fun putBegrunnelseSkalSetteBegrunnelse() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBegrunnelse("", "")

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val begrunnelseFrontend = BegrunnelseFrontend(SOKER_OM, SOKER_FORDI)
        begrunnelseRessurs.updateBegrunnelse(BEHANDLINGSID, begrunnelseFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val begrunnelse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.begrunnelse
        assertThat(begrunnelse.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(begrunnelse.hvaSokesOm).isEqualTo(SOKER_OM)
        assertThat(begrunnelse.hvorforSoke).isEqualTo(SOKER_FORDI)
    }

    @Test
    fun begrunnelseSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { begrunnelseRessurs.hentBegrunnelse(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putBegrunnelseSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val begrunnelseFrontend = BegrunnelseFrontend(SOKER_OM, SOKER_FORDI)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { begrunnelseRessurs.updateBegrunnelse(BEHANDLINGSID, begrunnelseFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun createJsonInternalSoknadWithBegrunnelse(hvaSokesOm: String, hvorforSoke: String): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.begrunnelse
            .withHvaSokesOm(hvaSokesOm)
            .withHvorforSoke(hvorforSoke)
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
        private const val SOKER_FORDI = "Jeg søker fordi..."
        private const val SOKER_OM = "Jeg søker om..."
    }
}
