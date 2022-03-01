package no.nav.sosialhjelp.soknad.personalia.familie

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
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag.Verdi
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.familie.dto.AnsvarFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.BarnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.ForsorgerpliktFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ForsorgerpliktRessursTest {

    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()

    private val forsorgerpliktRessurs = ForsorgerpliktRessurs(tilgangskontroll, textService, soknadUnderArbeidRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun forsorgerpliktSkalReturnereTomForsorgerplikt() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(null, null, null)

        val forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID)
        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isNull()
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull()
        assertThat(forsorgerpliktFrontend.ansvar).isNull()
    }

    @Test
    fun forsorgerpliktSkalReturnereEtBarnSomErFolkeregistrertSammenOgHarDeltBosted() {
        val jsonAnsvar = JsonAnsvar().withBarn(JSON_BARN)
            .withErFolkeregistrertSammen(
                JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(true)
            )
            .withHarDeltBosted(JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(true))
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(true, null, listOf(jsonAnsvar))

        val forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID)
        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull()
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(1)
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar!![0], jsonAnsvar)
    }

    @Test
    fun forsorgerpliktSkalReturnereEtBarnSomIkkeErFolkeregistrertSammenMenHarSamvarsgrad() {
        val jsonAnsvar = JsonAnsvar().withBarn(JSON_BARN)
            .withErFolkeregistrertSammen(
                JsonErFolkeregistrertSammen().withKilde(JsonKildeSystem.SYSTEM).withVerdi(false)
            )
            .withSamvarsgrad(JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(30))
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(true, null, listOf(jsonAnsvar))

        val forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID)
        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull()
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(1)
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar!![0], jsonAnsvar)
    }

    @Test
    fun forsorgerpliktSkalReturnereToBarn() {
        val jsonAnsvar = JsonAnsvar().withBarn(JSON_BARN)
        val jsonAnsvar_2 = JsonAnsvar().withBarn(JSON_BARN_2)
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(true, null, listOf(jsonAnsvar, jsonAnsvar_2))

        val forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID)
        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue
        assertThat(forsorgerpliktFrontend.barnebidrag).isNull()
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(2)
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar!![0], jsonAnsvar)
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar!![1], jsonAnsvar_2)
    }

    @Test
    fun forsorgerpliktSkalReturnereEtBarnOgBarnebidrag() {
        val jsonAnsvar = JsonAnsvar().withBarn(JSON_BARN)
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(true, Verdi.BEGGE, listOf(jsonAnsvar))

        val forsorgerpliktFrontend = forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID)
        assertThat(forsorgerpliktFrontend.harForsorgerplikt).isTrue
        assertThat(forsorgerpliktFrontend.barnebidrag).isEqualTo(Verdi.BEGGE)
        assertThat(forsorgerpliktFrontend.ansvar).hasSize(1)
        assertThatAnsvarIsCorrectlyConverted(forsorgerpliktFrontend.ansvar!![0], jsonAnsvar)
    }

    @Test
    fun putForsorgerpliktSkalSetteBarnebidrag() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(null, null, null)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val forsorgerpliktFrontend = ForsorgerpliktFrontend(null, Verdi.BETALER, null)
        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.barnebidrag.verdi).isEqualTo(Verdi.BETALER)
        assertThat(forsorgerplikt.harForsorgerplikt).isNull()
        assertThat(forsorgerplikt.ansvar).isNull()
    }

    @Test
    fun putForsorgerpliktSkalFjerneBarnebidragOgInntektOgUtgiftKnyttetTilBarnebidrag() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val soknad = createJsonInternalSoknadWithForsorgerplikt(null, Verdi.BEGGE, null)
        val inntekt: MutableList<JsonOkonomioversiktInntekt> = ArrayList()
        inntekt.add(JsonOkonomioversiktInntekt().withType("barnebidrag"))
        val utgift: MutableList<JsonOkonomioversiktUtgift> = ArrayList()
        utgift.add(JsonOkonomioversiktUtgift().withType("barnebidrag"))
        soknad.jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt = inntekt
        soknad.jsonInternalSoknad.soknad.data.okonomi.oversikt.utgift = utgift
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknad

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val forsorgerpliktFrontend = ForsorgerpliktFrontend(null, null, null)
        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie.forsorgerplikt
        val inntekter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt
        val utgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.utgift
        assertThat(forsorgerplikt.barnebidrag).isNull()
        assertThat(inntekter.isEmpty()).isTrue
        assertThat(utgifter.isEmpty()).isTrue
    }

    @Test
    fun putForsorgerpliktSkalSetteHarDeltBostedOgSamvarsgradPaaToBarn() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val jsonAnsvar = JsonAnsvar().withBarn(JSON_BARN)
        val jsonAnsvar_2 = JsonAnsvar().withBarn(JSON_BARN_2)
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithForsorgerplikt(true, null, listOf(jsonAnsvar, jsonAnsvar_2))

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val forsorgerpliktFrontend = ForsorgerpliktFrontend(null, null, listOf(createBarnMedDeltBosted(), createBarnMedSamvarsgrad()))
        forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val forsorgerplikt = soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie.forsorgerplikt
        assertThat(forsorgerplikt.barnebidrag).isNull()
        assertThat(forsorgerplikt.harForsorgerplikt.verdi).isTrue
        assertThat(forsorgerplikt.ansvar[0].harDeltBosted.verdi).isTrue
        assertThat(forsorgerplikt.ansvar[1].samvarsgrad.verdi).isEqualTo(30)
    }

    @Test
    fun forsorgerpliktSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { forsorgerpliktRessurs.hentForsorgerplikt(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putForsorgerpliktSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val forsorgerpliktFrontend = ForsorgerpliktFrontend(null, null, listOf(createBarnMedSamvarsgrad()))

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { forsorgerpliktRessurs.updateForsorgerplikt(BEHANDLINGSID, forsorgerpliktFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun createBarnMedSamvarsgrad(): AnsvarFrontend {
        return AnsvarFrontend(
            barn = BarnFrontend(
                navn = null,
                fodselsdato = null,
                personnummer = PersonMapper.getPersonnummerFromFnr(JSON_BARN_2.personIdentifikator),
                fodselsnummer = JSON_BARN_2.personIdentifikator
            ),
            borSammenMed = null,
            erFolkeregistrertSammen = null,
            harDeltBosted = null,
            samvarsgrad = 30
        )
    }

    private fun createBarnMedDeltBosted(): AnsvarFrontend {
        return AnsvarFrontend(
            barn = BarnFrontend(
                navn = null,
                fodselsdato = null,
                personnummer = PersonMapper.getPersonnummerFromFnr(JSON_BARN.personIdentifikator),
                fodselsnummer = JSON_BARN.personIdentifikator
            ),
            borSammenMed = null,
            erFolkeregistrertSammen = null,
            harDeltBosted = true,
            samvarsgrad = null
        )
    }

    private fun assertThatAnsvarIsCorrectlyConverted(
        ansvarFrontend: AnsvarFrontend?,
        jsonAnsvar: JsonAnsvar
    ) {
        val barnFrontend = ansvarFrontend?.barn
        val jsonBarn = jsonAnsvar.barn
        assertThat(ansvarFrontend?.borSammenMed)
            .isEqualTo(if (jsonAnsvar.borSammenMed == null) null else jsonAnsvar.borSammenMed.verdi)
        assertThat(ansvarFrontend?.harDeltBosted)
            .isEqualTo(if (jsonAnsvar.harDeltBosted == null) null else jsonAnsvar.harDeltBosted.verdi)
        assertThat(ansvarFrontend?.samvarsgrad)
            .isEqualTo(if (jsonAnsvar.samvarsgrad == null) null else jsonAnsvar.samvarsgrad.verdi)
        assertThat(ansvarFrontend?.erFolkeregistrertSammen)
            .isEqualTo(if (jsonAnsvar.erFolkeregistrertSammen == null) null else jsonAnsvar.erFolkeregistrertSammen.verdi)
        assertThat(barnFrontend?.fodselsnummer).isEqualTo(jsonBarn.personIdentifikator)
        assertThat(barnFrontend?.fodselsdato).isEqualTo(jsonBarn.fodselsdato)
        assertThat(barnFrontend?.navn?.fornavn).isEqualTo(jsonBarn.navn.fornavn)
        assertThat(barnFrontend?.navn?.mellomnavn).isEqualTo(jsonBarn.navn.mellomnavn)
        assertThat(barnFrontend?.navn?.etternavn).isEqualTo(jsonBarn.navn.etternavn)
    }

    private fun createJsonInternalSoknadWithForsorgerplikt(
        harForsorgerplikt: Boolean?,
        barnebidrag: Verdi?,
        ansvars: List<JsonAnsvar>?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie
            .withForsorgerplikt(
                JsonForsorgerplikt()
                    .withHarForsorgerplikt(
                        if (harForsorgerplikt == null) null else JsonHarForsorgerplikt()
                            .withKilde(JsonKilde.SYSTEM)
                            .withVerdi(harForsorgerplikt)
                    )
                    .withBarnebidrag(
                        if (barnebidrag == null) null else JsonBarnebidrag()
                            .withKilde(JsonKildeBruker.BRUKER)
                            .withVerdi(barnebidrag)
                    )
                    .withAnsvar(ansvars)
            )
        return soknadUnderArbeid
    }

    companion object {
        private const val EIER = "123456789101"
        private const val BEHANDLINGSID = "123"
        private val JSON_BARN = JsonBarn()
            .withKilde(JsonKilde.SYSTEM)
            .withNavn(
                JsonNavn()
                    .withFornavn("Amadeus")
                    .withMellomnavn("Wolfgang")
                    .withEtternavn("Mozart")
            )
            .withFodselsdato("1756-01-27")
            .withPersonIdentifikator("11111111111")
        private val JSON_BARN_2 = JsonBarn()
            .withKilde(JsonKilde.SYSTEM)
            .withNavn(
                JsonNavn()
                    .withFornavn("Ludwig")
                    .withMellomnavn("van")
                    .withEtternavn("Beethoven")
            )
            .withFodselsdato("1770-12-16")
            .withPersonIdentifikator("22222222222")
    }
}
