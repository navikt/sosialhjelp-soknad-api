package no.nav.sosialhjelp.soknad.inntekt.studielan

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.studielan.StudielanRessurs.StudielanFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class StudielanRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()
    private val studielanRessurs = StudielanRessurs(tilgangskontroll, soknadUnderArbeidRepository, textService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

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
    fun studielanSkalReturnereNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, null)

        val studielanFrontend: StudielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID)
        assertThat(studielanFrontend.skalVises).isTrue
        assertThat(studielanFrontend.bekreftelse).isNull()
    }

    @Test
    fun studielanSkalReturnereBekreftetStudielan() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, true)

        val studielanFrontend: StudielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID)
        assertThat(studielanFrontend.skalVises).isTrue
        assertThat(studielanFrontend.bekreftelse).isTrue
    }

    @Test
    fun studielanSkalReturnereHarIkkeStudielan() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithErStudentStudielanBekreftelse(true, false)

        val studielanFrontend: StudielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID)
        assertThat(studielanFrontend.skalVises).isTrue
        assertThat(studielanFrontend.bekreftelse).isFalse
    }

    @Test
    fun studielanSkalReturnereSkalIkkeVisesHvisIkkeStudent() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithErStudentStudielanBekreftelse(false, null)

        val studielanFrontend: StudielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID)
        assertThat(studielanFrontend.skalVises).isFalse
        assertThat(studielanFrontend.bekreftelse).isNull()
    }

    @Test
    fun studielanSkalReturnereSkalIkkeVisesHvisStudentSporsmalIkkeBesvart() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithErStudentStudielanBekreftelse(null, null)

        val studielanFrontend: StudielanFrontend = studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID)
        assertThat(studielanFrontend.skalVises).isFalse
        assertThat(studielanFrontend.bekreftelse).isNull()
    }

    @Test
    fun putStudielanSkalSetteStudielanOgLeggeTilInntektstypen() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid()
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val studielanFrontend = StudielanFrontend(false, true)
        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data
            .okonomi.opplysninger.bekreftelse
        val inntekt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data
            .okonomi.oversikt.inntekt
        assertThat(inntekt[0].type).isEqualTo(SoknadJsonTyper.STUDIELAN)

        val studielan = bekreftelser[0]
        assertThat(studielan.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(studielan.type).isEqualTo(SoknadJsonTyper.STUDIELAN)
        assertThat(studielan.verdi).isTrue
    }

    @Test
    fun putStudielanSkalSetteHarIkkeStudielanOgSletteInntektstypen() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val soknad = createSoknadUnderArbeid()
        val inntekt = ArrayList<JsonOkonomioversiktInntekt>()
        inntekt.add(JsonOkonomioversiktInntekt().withType(SoknadJsonTyper.STUDIELAN))
        soknad.jsonInternalSoknad!!.soknad.data.okonomi.oversikt.inntekt = inntekt
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknad
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val studielanFrontend = StudielanFrontend(false, false)
        studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse
        val jsonInntekt = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.oversikt.inntekt
        assertThat(jsonInntekt).isEmpty()

        val studielan = bekreftelser[0]
        assertThat(studielan.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(studielan.type).isEqualTo(SoknadJsonTyper.STUDIELAN)
        assertThat(studielan.verdi).isFalse
    }

    @Test
    fun studielanSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { studielanRessurs.hentStudielanBekreftelse(BEHANDLINGSID) }
        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putStudielanSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")
        val studielanFrontend = StudielanFrontend(false, null)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { studielanRessurs.updateStudielan(BEHANDLINGSID, studielanFrontend) }
        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    private fun createJsonInternalSoknadWithErStudentStudielanBekreftelse(
        erStudent: Boolean?,
        verdi: Boolean?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.withBekreftelse(
            listOf(
                JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(SoknadJsonTyper.STUDIELAN)
                    .withVerdi(verdi)
            )
        )
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.utdanning.erStudent = erStudent
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = BEHANDLINGSID,
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
