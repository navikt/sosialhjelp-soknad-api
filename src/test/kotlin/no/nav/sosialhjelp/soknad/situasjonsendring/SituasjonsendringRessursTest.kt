package no.nav.sosialhjelp.soknad.situasjonsendring

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.situasjonendring.JsonSituasjonendring
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.v2.shadow.V2ControllerAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

private const val BEHANDLINGSID = "123"
private const val EIER = "123456789101"

class SituasjonsendringRessursTest {
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val controllerAdapter: V2ControllerAdapter = mockk()
    private val situasjonsendringRessurs = SituasjonsendringRessurs(tilgangskontroll, soknadUnderArbeidRepository)

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { controllerAdapter.updateSituasjonsendring(any(), any()) } just runs
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun `skal returnere situasjonendring`() {
        every { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithSituasjonsendring(true, "Noe har endret seg")

        val situasjonsendring = situasjonsendringRessurs.hentSituasjonsendring(BEHANDLINGSID)
        assertThat(situasjonsendring.endring).isEqualTo(true)
        assertThat(situasjonsendring.hvaErEndret).isEqualTo("Noe har endret seg")
    }

    @Test
    fun `skal oppdatere situasjonendring`() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithSituasjonsendring(null, null)

        val slot = slot<SoknadUnderArbeid>()
        every {
            soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any())
        } just runs

        situasjonsendringRessurs.updateSituasjonsendring(BEHANDLINGSID, SituasjonsendringFrontend(true, "Det er noe nytt her"))

        val captured =
            slot.captured.jsonInternalSoknad
                ?.soknad
                ?.data
                ?.situasjonendring
        assertThat(captured?.hvaHarEndretSeg).isEqualTo("Det er noe nytt her")
    }

    private fun createJsonInternalSoknadWithSituasjonsendring(
        endring: Boolean?,
        hvaErEndret: String?,
    ): SoknadUnderArbeid {
        val soknadUnderArbeid =
            SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER, true),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )
        soknadUnderArbeid.jsonInternalSoknad!!
            .soknad.data.situasjonendring =
            JsonSituasjonendring()
                .withHarNoeEndretSeg(endring)
                .withHvaHarEndretSeg(hvaErEndret)
                .withKilde(JsonKildeBruker.BRUKER)
        return soknadUnderArbeid
    }
}
