package no.nav.sosialhjelp.soknad.inntekt.formue

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.formue.FormueRessurs.FormueFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FormueRessursTest {

    private val BEHANDLINGSID = "123"
    private val EIER = "123456789101"

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()
    private val formueRessurs = FormueRessurs(tilgangskontroll, soknadUnderArbeidRepository, textService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    fun getFormueSkalReturnereBekreftelserLikFalse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID)
        assertThat(formueFrontend.brukskonto).isFalse
        assertThat(formueFrontend.bsu).isFalse
        assertThat(formueFrontend.livsforsikring).isFalse
        assertThat(formueFrontend.sparekonto).isFalse
        assertThat(formueFrontend.verdipapirer).isFalse
        assertThat(formueFrontend.annet).isFalse
        assertThat(formueFrontend.beskrivelseAvAnnet).isNull()
    }

    @Test
    fun getFormueSkalReturnereBekreftelserLikTrue() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithFormue(
            listOf(
                SoknadJsonTyper.FORMUE_BRUKSKONTO,
                SoknadJsonTyper.FORMUE_BSU,
                SoknadJsonTyper.FORMUE_LIVSFORSIKRING,
                SoknadJsonTyper.FORMUE_VERDIPAPIRER,
                SoknadJsonTyper.FORMUE_SPAREKONTO,
                SoknadJsonTyper.FORMUE_ANNET
            ),
            null
        )

        val formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID)
        assertThat(formueFrontend.brukskonto).isTrue
        assertThat(formueFrontend.bsu).isTrue
        assertThat(formueFrontend.livsforsikring).isTrue
        assertThat(formueFrontend.sparekonto).isTrue
        assertThat(formueFrontend.verdipapirer).isTrue
        assertThat(formueFrontend.annet).isTrue
        assertThat(formueFrontend.beskrivelseAvAnnet).isNull()
    }

    @Test
    fun getFormueSkalReturnereBeskrivelseAvAnnet() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val beskrivelse = "Vinylplater"
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithFormue(listOf(SoknadJsonTyper.FORMUE_ANNET), beskrivelse)

        val formueFrontend = formueRessurs.hentFormue(BEHANDLINGSID)
        assertThat(formueFrontend.annet).isTrue
        assertThat(formueFrontend.beskrivelseAvAnnet).isEqualTo(beskrivelse)
    }

    @Test
    fun putFormueSkalSetteAlleBekreftelserLikFalse() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithFormue(
                listOf(
                    SoknadJsonTyper.FORMUE_BRUKSKONTO,
                    SoknadJsonTyper.FORMUE_BSU,
                    SoknadJsonTyper.FORMUE_LIVSFORSIKRING,
                    SoknadJsonTyper.FORMUE_VERDIPAPIRER,
                    SoknadJsonTyper.FORMUE_SPAREKONTO,
                    SoknadJsonTyper.FORMUE_ANNET
                ),
                "Vinylplater"
            )
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val formueFrontend = FormueFrontend(beskrivelseAvAnnet = null)
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val sparing = bekreftelser[0]
        val formuer = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        val beskrivelse =
            soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.sparing
        assertThat(sparing.verdi).isFalse
        assertThat(formuer).isEmpty()
        assertThat(beskrivelse).isBlank
    }

    @Test
    fun putFormueSkalSetteNoenBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val formueFrontend = FormueFrontend(
            brukskonto = true,
            sparekonto = false,
            bsu = true,
            livsforsikring = true,
            verdipapirer = false,
            annet = false,
            beskrivelseAvAnnet = null
        )
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val sparing = bekreftelser[0]
        val formuer = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        assertThat(sparing.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(sparing.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_SPARING)
        assertThat(sparing.verdi).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_BRUKSKONTO }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_BSU }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_LIVSFORSIKRING }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_SPAREKONTO }).isFalse
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_VERDIPAPIRER }).isFalse
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_ANNET }).isFalse
    }

    @Test
    fun putFormueSkalSetteAlleBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val formueFrontend = FormueFrontend(
            brukskonto = true,
            sparekonto = true,
            bsu = true,
            livsforsikring = true,
            verdipapirer = true,
            annet = true,
            beskrivelseAvAnnet = null
        )
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val sparing = bekreftelser[0]
        val formuer = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        assertThat(sparing.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(sparing.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_SPARING)
        assertThat(sparing.verdi).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_BRUKSKONTO }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_BSU }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_LIVSFORSIKRING }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_SPAREKONTO }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_VERDIPAPIRER }).isTrue
        assertThat(formuer.any { it.type == SoknadJsonTyper.FORMUE_ANNET }).isTrue
    }

    @Test
    fun putFormueSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithFormue(listOf(SoknadJsonTyper.FORMUE_ANNET), "Vinylplater")
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val formueFrontend = FormueFrontend(beskrivelseAvAnnet = null)
        formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val sparing = bekreftelser[0]
        val beskrivelse =
            soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.sparing
        assertThat(sparing.verdi).isFalse
        assertThat(beskrivelse).isBlank
    }

    @Test
    fun getFormueSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { formueRessurs.hentFormue(BEHANDLINGSID) }
        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putFormueSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val formueFrontend = FormueFrontend(beskrivelseAvAnnet = null)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { formueRessurs.updateFormue(BEHANDLINGSID, formueFrontend) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    private fun createJsonInternalSoknadWithFormue(
        formueTyper: List<String>,
        beskrivelseAvAnnet: String?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val formuer = formueTyper.map {
            JsonOkonomioversiktFormue()
                .withKilde(JsonKilde.BRUKER)
                .withType(it)
                .withTittel("tittel")
        }

        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue = formuer
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet =
            JsonOkonomibeskrivelserAvAnnet().withSparing(beskrivelseAvAnnet)
        return soknadUnderArbeid
    }
}
