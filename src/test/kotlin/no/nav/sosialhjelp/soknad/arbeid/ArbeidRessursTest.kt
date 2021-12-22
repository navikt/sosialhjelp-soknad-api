package no.nav.sosialhjelp.soknad.arbeid

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs.ArbeidFrontend
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ArbeidRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val arbeidRessurs = ArbeidRessurs(soknadUnderArbeidRepository, tilgangskontroll)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        System.clearProperty("environment.name")
    }

    @Test
    fun arbeidSkalReturnereSystemArbeidsforholdRiktigKonvertert() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(createArbeidsforholdListe(), null)

        val arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID)
        val arbeidsforholdFrontends = arbeidFrontend.arbeidsforhold
        assertThat(arbeidsforholdFrontends).hasSize(2)

        val arbeidsforhold_1 = arbeidsforholdFrontends!![0]
        val arbeidsforhold_2 = arbeidsforholdFrontends[1]
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_1, ARBEIDSFORHOLD_1)
        assertThatArbeidsforholdIsCorrectlyConverted(arbeidsforhold_2, ARBEIDSFORHOLD_2)
    }

    @Test
    fun arbeidSkalReturnereArbeidsforholdLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, null)

        val arbeidFrontend = arbeidRessurs.hentArbeid(BEHANDLINGSID)
        assertThat(arbeidFrontend.arbeidsforhold).isNull()
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
    fun putArbeidSkalLageNyJsonKommentarTilArbeidsforholdDersomDenVarNull() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, null)
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidFrontend = ArbeidFrontend(null, KOMMENTAR)
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend)

        val soknadUnderArbeid = slot.captured
        val kommentarTilArbeidsforhold = soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid.kommentarTilArbeidsforhold
        assertThat(kommentarTilArbeidsforhold.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(kommentarTilArbeidsforhold.verdi).isEqualTo(KOMMENTAR)
    }

    @Test
    fun putArbeidSkalOppdatereKommentarTilArbeidsforhold() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar")
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidFrontend = ArbeidFrontend(null, KOMMENTAR)
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend)

        val soknadUnderArbeid = slot.captured
        val kommentarTilArbeidsforhold = soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid.kommentarTilArbeidsforhold
        assertThat(kommentarTilArbeidsforhold.kilde).isEqualTo(JsonKildeBruker.BRUKER)
        assertThat(kommentarTilArbeidsforhold.verdi).isEqualTo(KOMMENTAR)
    }

    @Test
    fun putArbeidSkalSetteLikNullDersomKommentarenErTom() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithArbeid(null, "Tidligere kommentar")
        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val arbeidFrontend = ArbeidFrontend(null, "")
        arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend)

        val soknadUnderArbeid = slot.captured
        val kommentarTilArbeidsforhold = soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid.kommentarTilArbeidsforhold
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

        val arbeidFrontend = ArbeidFrontend(null, "")

        assertThatCode { arbeidRessurs.updateArbeid(BEHANDLINGSID, arbeidFrontend) }.isInstanceOf(AuthorizationException::class.java)

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
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid
            .withForhold(arbeidsforholdList)
            .withKommentarTilArbeidsforhold(
                if (kommentar == null) null else JsonKommentarTilArbeidsforhold()
                    .withKilde(JsonKildeBruker.BRUKER)
                    .withVerdi(kommentar)
            )
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
