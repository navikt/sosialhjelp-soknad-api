package no.nav.sosialhjelp.soknad.arbeid

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs.ArbeidsforholdRequest
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ArbeidRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val arbeidRessurs = ArbeidRessurs(soknadUnderArbeidRepository, tilgangskontroll)

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
    fun arbeidSkalReturnereSystemArbeidsforholdRiktigKonvertert() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(createArbeidsforholdListe(), null)

        val arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID)
        val arbeidsforholdFrontends = arbeidFrontend.arbeidsforhold
        assertThat(arbeidsforholdFrontends).hasSize(2)

        val arbeidsforhold1 = arbeidsforholdFrontends[0]
        val arbeidsforhold2 = arbeidsforholdFrontends[1]
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold1, ARBEIDSFORHOLD_1)
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold2, ARBEIDSFORHOLD_2)
    }

    @Test
    fun arbeidSkalReturnereArbeidsforholdLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, null)

        val arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID)
        assertThat(arbeidFrontend.arbeidsforhold).isEmpty()
    }

    @Test
    fun arbeidSkalReturnereKommentarTilArbeidsforholdLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, null)

        val arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID)
        assertThat(arbeidFrontend.kommentarTilArbeidsforhold).isNull()
    }

    @Test
    fun arbeidSkalReturnereKommentarTilArbeidsforhold() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, KOMMENTAR)

        val arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID)
        assertThat(arbeidFrontend.kommentarTilArbeidsforhold).isEqualTo(KOMMENTAR)
    }

    @Test
    fun `putArbeid skal lage ny JsonKommentarTilArbeidsforhold dersom den var null`() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, null)
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidsforholdRequest = ArbeidsforholdRequest(KOMMENTAR)
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidsforholdRequest)

        val soknadUnderArbeid = slot.captured
        val internalSoknad = soknadUnderArbeid.jsonInternalSoknad
        val kommentarTilArbeidsforhold = internalSoknad!!.soknad.data.arbeid.kommentarTilArbeidsforhold
        assertThat(kommentarTilArbeidsforhold.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(kommentarTilArbeidsforhold.verdi).isEqualTo(KOMMENTAR)

        val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        assertThatNoException().isThrownBy {
            JsonSosialhjelpValidator.ensureValidInternalSoknad(mapper.writeValueAsString(internalSoknad))
        }
    }

    @Test
    fun `putArbeid med KommentarTilArbeidsfohrold lager json som ikke validerer hvis forhold-liste har blitt satt lik null`() {
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
        // skal ikke være mulig:
        soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.arbeid?.forhold = null

        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidsforholdRequest = ArbeidsforholdRequest(KOMMENTAR)
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidsforholdRequest)

        val captured = slot.captured
        val internalSoknad = captured.jsonInternalSoknad
        val kommentarTilArbeidsforhold = internalSoknad!!.soknad.data.arbeid.kommentarTilArbeidsforhold
        assertThat(kommentarTilArbeidsforhold.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(kommentarTilArbeidsforhold.verdi).isEqualTo(KOMMENTAR)

        val mapper = JsonSosialhjelpObjectMapper.createObjectMapper()
        assertThatExceptionOfType(JsonSosialhjelpValidationException::class.java).isThrownBy {
            JsonSosialhjelpValidator.ensureValidInternalSoknad(mapper.writeValueAsString(internalSoknad))
        }
    }

    @Test
    fun putArbeidSkalOppdatereKommentarTilArbeidsforhold() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar")
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidsforholdRequest = ArbeidsforholdRequest(KOMMENTAR)
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidsforholdRequest)

        val soknadUnderArbeid = slot.captured
        val kommentarTilArbeidsforhold = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.arbeid.kommentarTilArbeidsforhold
        assertThat(kommentarTilArbeidsforhold.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(kommentarTilArbeidsforhold.verdi).isEqualTo(KOMMENTAR)
    }

    @Test
    fun putArbeidSkalSetteLikNullDersomKommentarenErTom() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar")
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidsforholdRequest = ArbeidsforholdRequest("")
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidsforholdRequest)

        val soknadUnderArbeid = slot.captured
        val kommentarTilArbeidsforhold = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.arbeid.kommentarTilArbeidsforhold
        assertThat(kommentarTilArbeidsforhold).isNull()
    }

    @Test
    fun arbeidSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatCode { arbeidRessurs.hentArbeid(BEHANDLINGSID) }.isInstanceOf(AuthorizationException::class.java)

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putArbeidSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID) } throws AuthorizationException("Not for you my friend")

        val arbeidsforholdRequest = ArbeidsforholdRequest("")

        assertThatCode { arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidsforholdRequest) }.isInstanceOf(AuthorizationException::class.java)

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    private fun assertThatArbeidsforholdIsCorrectlyConverted(
        forholdFrontend: ArbeidRessurs.ArbeidsforholdFrontend,
        jsonForhold: JsonArbeidsforhold
    ) {
        assertThat(forholdFrontend.arbeidsgivernavn).isEqualTo(jsonForhold.arbeidsgivernavn)
        assertThat(forholdFrontend.fom).isEqualTo(jsonForhold.fom)
        assertThat(forholdFrontend.tom).isEqualTo(jsonForhold.tom)
        assertThat(forholdFrontend.stillingsprosent).isEqualTo(jsonForhold.stillingsprosent)
        assertThatStillingstypeIsCorrect(forholdFrontend.stillingstypeErHeltid, jsonForhold.stillingstype)
        assertThat(forholdFrontend.overstyrtAvBruker).isEqualTo(java.lang.Boolean.FALSE)
    }

    private fun assertThatStillingstypeIsCorrect(stillingstypeErHeltid: Boolean?, stillingstype: Stillingstype) {
        if (stillingstypeErHeltid == null) {
            return
        }
        if (stillingstypeErHeltid) {
            assertThat(stillingstype).isEqualTo(Stillingstype.FAST)
        } else {
            assertThat(stillingstype).isEqualTo(Stillingstype.VARIABEL)
        }
    }

    private fun createArbeidsforholdListe(): List<JsonArbeidsforhold> {
        return mutableListOf(ARBEIDSFORHOLD_1, ARBEIDSFORHOLD_2)
    }

    private fun createJsonInternalSoknadWithArbeid(
        arbeidsforholdList: List<JsonArbeidsforhold>?,
        kommentar: String?
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
        arbeidsforholdList?.let { soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.arbeid?.forhold = it }
        kommentar?.let {
            soknadUnderArbeid.jsonInternalSoknad?.soknad?.data?.arbeid?.kommentarTilArbeidsforhold =
                JsonKommentarTilArbeidsforhold()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(kommentar)
        }
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
        private const val KOMMENTAR =
            "Hath not the potter power over the clay, to make one vessel unto honor and another unto dishonor?"
        private val ARBEIDSFORHOLD_1 = JsonArbeidsforhold()
            .withKilde(JsonKilde.SYSTEM)
            .withArbeidsgivernavn("Good Corp.")
            .withFom("1337-01-01")
            .withTom("2020-01-01")
            .withStillingstype(Stillingstype.FAST)
            .withStillingsprosent(50)
            .withOverstyrtAvBruker(java.lang.Boolean.FALSE)
        private val ARBEIDSFORHOLD_2 = JsonArbeidsforhold()
            .withKilde(JsonKilde.SYSTEM)
            .withArbeidsgivernavn("Evil Corp.")
            .withFom("1337-02-02")
            .withTom("2020-02-02")
            .withStillingstype(Stillingstype.VARIABEL)
            .withStillingsprosent(30)
            .withOverstyrtAvBruker(java.lang.Boolean.FALSE)
    }
}
