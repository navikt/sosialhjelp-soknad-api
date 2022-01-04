package no.nav.sosialhjelp.soknad.inntekt.verdi

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_BOLIG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_CAMPINGVOGN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_FRITIDSEIENDOM
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.VERDI_KJORETOY
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.verdi.VerdiRessurs.VerdierFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VerdiRessursTest {

    private val BEHANDLINGSID = "123"
    private val EIER = "123456789101"

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()
    private val verdiRessurs = VerdiRessurs(tilgangskontroll, soknadUnderArbeidRepository, textService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        System.setProperty("environment.name", "test")
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        System.clearProperty("environment.name")
    }

    @Test
    fun getVerdierSkalReturnereBekreftelseLikNullOgAltFalse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID)
        assertThat(verdierFrontend.bekreftelse).isNull()
        assertThat(verdierFrontend.bolig).isFalse
        assertThat(verdierFrontend.campingvogn).isFalse
        assertThat(verdierFrontend.kjoretoy).isFalse
        assertThat(verdierFrontend.fritidseiendom).isFalse
        assertThat(verdierFrontend.annet).isFalse
        assertThat(verdierFrontend.beskrivelseAvAnnet).isNull()
    }

    @Test
    fun getVerdierSkalReturnereBekreftelserLikTrue() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithVerdier(
                true,
                listOf(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY, VERDI_FRITIDSEIENDOM, VERDI_ANNET),
                null
            )

        val verdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID)
        assertThat(verdierFrontend.bekreftelse).isTrue
        assertThat(verdierFrontend.bolig).isTrue
        assertThat(verdierFrontend.campingvogn).isTrue
        assertThat(verdierFrontend.kjoretoy).isTrue
        assertThat(verdierFrontend.fritidseiendom).isTrue
        assertThat(verdierFrontend.annet).isTrue
        assertThat(verdierFrontend.beskrivelseAvAnnet).isNull()
    }

    @Test
    fun getVerdierSkalReturnereBeskrivelseAvAnnet() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val beskrivelse = "Bestefars klokke"
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithVerdier(true, listOf(VERDI_ANNET), beskrivelse)

        val verdierFrontend: VerdierFrontend = verdiRessurs.hentVerdier(BEHANDLINGSID)
        assertThat(verdierFrontend.bekreftelse).isTrue
        assertThat(verdierFrontend.annet).isTrue
        assertThat(verdierFrontend.beskrivelseAvAnnet).isEqualTo(beskrivelse)
    }

    @Test
    fun putVerdierSkalSetteAltFalseDersomManVelgerHarIkkeVerdier() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithVerdier(
                true,
                listOf(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY, VERDI_ANNET),
                "Bestefars klokke"
            )
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val verdierFrontend = VerdierFrontend(bekreftelse = false)
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val verdiBekreftelse = bekreftelser[0]
        val verdier = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        assertThat(verdiBekreftelse.verdi).isFalse
        assertThat(verdier.isEmpty()).isTrue
    }

    @Test
    fun putVerdierSkalSetteAlleBekreftelserLikFalse() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithVerdier(
                true,
                listOf(VERDI_BOLIG, VERDI_CAMPINGVOGN, VERDI_KJORETOY, VERDI_ANNET),
                "Bestefars klokke"
            )
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val verdierFrontend = VerdierFrontend(bekreftelse = false)
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val verdiBekreftelse = bekreftelser[0]
        val verdier = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        val beskrivelse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.verdi
        assertThat(verdiBekreftelse.verdi).isFalse
        assertThat(verdier.isEmpty()).isTrue
        assertThat(beskrivelse).isBlank
    }

    @Test
    fun putVerdierSkalSetteNoenBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val verdierFrontend = VerdierFrontend(
            bekreftelse = true,
            bolig = true,
            campingvogn = true,
            kjoretoy = false,
            fritidseiendom = false,
            annet = false
        )
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val verdiBekreftelse = bekreftelser[0]
        val verdier = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        assertThat(verdiBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(verdiBekreftelse.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_VERDI)
        assertThat(verdiBekreftelse.verdi).isTrue
        assertThat(verdier.any { it.type == VERDI_BOLIG }).isTrue
        assertThat(verdier.any { it.type == VERDI_CAMPINGVOGN }).isTrue
        assertThat(verdier.any { it.type == VERDI_KJORETOY }).isFalse
        assertThat(verdier.any { it.type == VERDI_FRITIDSEIENDOM }).isFalse
        assertThat(verdier.any { it.type == VERDI_ANNET }).isFalse
    }

    @Test
    fun putVerdierSkalSetteAlleBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val verdierFrontend = VerdierFrontend(
            bekreftelse = true,
            bolig = true,
            campingvogn = true,
            kjoretoy = true,
            fritidseiendom = true,
            annet = true
        )
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val verdiBekreftelse = bekreftelser[0]
        val verdier = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue
        assertThat(verdiBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(verdiBekreftelse.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_VERDI)
        assertThat(verdiBekreftelse.verdi).isTrue
        assertThat(verdier.any { it.type == VERDI_KJORETOY }).isTrue
        assertThat(verdier.any { it.type == VERDI_CAMPINGVOGN }).isTrue
        assertThat(verdier.any { it.type == VERDI_BOLIG }).isTrue
        assertThat(verdier.any { it.type == VERDI_FRITIDSEIENDOM }).isTrue
        assertThat(verdier.any { it.type == VERDI_ANNET }).isTrue
    }

    @Test
    fun putVerdierSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithVerdier(true, listOf(VERDI_ANNET), "Vinylplater")
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val verdierFrontend = VerdierFrontend(bekreftelse = false)
        verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val verdiBekreftelse = bekreftelser[0]
        val beskrivelse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.verdi
        assertThat(verdiBekreftelse.verdi).isFalse
        assertThat(beskrivelse).isBlank
    }

    @Test
    fun getVerdierSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { verdiRessurs.hentVerdier(BEHANDLINGSID) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putVerdierSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID) } throws AuthorizationException("Not for you my friend")
        val verdierFrontend = VerdierFrontend()
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { verdiRessurs.updateVerdier(BEHANDLINGSID, verdierFrontend) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    private fun createJsonInternalSoknadWithVerdier(
        harVerdier: Boolean,
        verdiTyper: List<String>,
        beskrivelseAvAnnet: String?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val verdier: MutableList<JsonOkonomioversiktFormue> = ArrayList()
        for (verdi in verdiTyper) {
            verdier.add(
                JsonOkonomioversiktFormue()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(verdi)
                    .withTittel("tittel")
            )
        }
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse = listOf(
            JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(SoknadJsonTyper.BEKREFTELSE_VERDI)
                .withVerdi(harVerdier)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.formue = verdier
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet =
            JsonOkonomibeskrivelserAvAnnet().withVerdi(beskrivelseAvAnnet)
        return soknadUnderArbeid
    }
}
