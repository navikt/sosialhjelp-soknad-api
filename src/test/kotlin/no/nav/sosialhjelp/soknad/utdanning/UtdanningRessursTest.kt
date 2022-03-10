package no.nav.sosialhjelp.soknad.utdanning

import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning.Studentgrad
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.utdanning.UtdanningRessurs.UtdanningFrontend
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class UtdanningRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()

    private val utdanningRessurs = UtdanningRessurs(tilgangskontroll, soknadUnderArbeidRepository)

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
    fun utdanningSkalReturnereUtdanningUtenErStudentOgStudentgrad() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(null, null)

        val utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID)
        assertThat(utdanningFrontend.erStudent).isNull()
        assertThat(utdanningFrontend.studengradErHeltid).isNull()
    }

    @Test
    fun utdanningSkalReturnereUtdanningMedErIkkeStudent() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(java.lang.Boolean.FALSE, null)

        val utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID)
        assertThat(utdanningFrontend.erStudent).isFalse
        assertThat(utdanningFrontend.studengradErHeltid).isNull()
    }

    @Test
    fun utdanningSkalReturnereUtdanningMedErStudent() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(java.lang.Boolean.TRUE, null)

        val utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID)
        assertThat(utdanningFrontend.erStudent).isTrue
        assertThat(utdanningFrontend.studengradErHeltid).isNull()
    }

    @Test
    fun utdanningSkalReturnereUtdanningMedErStudentOgStudentgradHeltid() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(java.lang.Boolean.TRUE, Studentgrad.HELTID)

        val utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID)
        assertThat(utdanningFrontend.erStudent).isTrue
        assertThat(utdanningFrontend.studengradErHeltid).isTrue
    }

    @Test
    fun utdanningSkalReturnereUtdanningMedErStudentOgStudentgradDeltid() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(true, Studentgrad.DELTID)

        val utdanningFrontend = utdanningRessurs.hentUtdanning(BEHANDLINGSID)
        assertThat(utdanningFrontend.erStudent).isTrue
        assertThat(utdanningFrontend.studengradErHeltid).isFalse
    }

    @Test
    fun putUtdanningSkalSetteUtdanningMedErStudent() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(null, null)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val utdanningFrontend = UtdanningFrontend(erStudent = true, studengradErHeltid = null)
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val utdanning = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.utdanning
        assertThat(utdanning.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utdanning.erStudent).isTrue
        assertThat(utdanning.studentgrad).isNull()
    }

    @Test
    fun putUtdanningSkalSetteUtdanningMedErStudentOgStudentgrad() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(null, null)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val utdanningFrontend = UtdanningFrontend(erStudent = true, studengradErHeltid = true)
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val utdanning = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.utdanning
        assertThat(utdanning.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utdanning.erStudent).isTrue
        assertThat(utdanning.studentgrad).isEqualTo(Studentgrad.HELTID)
    }

    @Test
    fun putUtdanningSkalSetteUtdanningMedErIkkeStudentOgSletteStudentgrad() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithUtdanning(true, Studentgrad.DELTID)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val utdanningFrontend = UtdanningFrontend(erStudent = false, studengradErHeltid = false)
        utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val utdanning = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.utdanning
        assertThat(utdanning.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utdanning.erStudent).isFalse
        assertThat(utdanning.studentgrad).isNull()
    }

    @Test
    fun utdanningSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { utdanningRessurs.hentUtdanning(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putUtdanningSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val utdanningFrontend = UtdanningFrontend(null, null)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { utdanningRessurs.updateUtdanning(BEHANDLINGSID, utdanningFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun createJsonInternalSoknadWithUtdanning(
        erStudent: Boolean?,
        studentgrad: Studentgrad?
    ): SoknadUnderArbeid {
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
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.utdanning
            .withKilde(JsonKilde.BRUKER)
            .withErStudent(erStudent)
            .withStudentgrad(studentgrad)
        return soknadUnderArbeid
    }

    companion object {
        private const val EIER = "12345678910"
        private const val BEHANDLINGSID = "123"
    }
}
