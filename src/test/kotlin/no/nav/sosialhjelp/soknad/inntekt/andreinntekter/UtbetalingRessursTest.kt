package no.nav.sosialhjelp.soknad.inntekt.andreinntekter

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
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_ANNET
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_FORSIKRING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SALG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_UTBYTTE
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.andreinntekter.UtbetalingRessurs.UtbetalingerFrontend
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UtbetalingRessursTest {

    private val BEHANDLINGSID = "123"
    private val EIER = "123456789101"

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()
    private val utbetalingRessurs = UtbetalingRessurs(tilgangskontroll, soknadUnderArbeidRepository, textService)

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
    fun getUtbetalingerSkalReturnereBekreftelseLikNullOgAltFalse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID)
        assertThat(utbetalingerFrontend.bekreftelse).isNull()
        assertThat(utbetalingerFrontend.forsikring).isFalse
        assertThat(utbetalingerFrontend.salg).isFalse
        assertThat(utbetalingerFrontend.utbytte).isFalse
        assertThat(utbetalingerFrontend.annet).isFalse
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet).isNull()
    }

    @Test
    fun getUtbetalingerSkalReturnereBekreftelserLikTrue() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithUtbetalinger(
            true, listOf(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING, UTBETALING_ANNET), null
        )

        val utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID)
        assertThat(utbetalingerFrontend.bekreftelse).isTrue
        assertThat(utbetalingerFrontend.utbytte).isTrue
        assertThat(utbetalingerFrontend.salg).isTrue
        assertThat(utbetalingerFrontend.forsikring).isTrue
        assertThat(utbetalingerFrontend.annet).isTrue
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet).isNull()
    }

    @Test
    fun getUtbetalingerSkalReturnereBeskrivelseAvAnnet() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val beskrivelse = "Lottogevinst"
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithUtbetalinger(true, listOf(UTBETALING_ANNET), beskrivelse)

        val utbetalingerFrontend = utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID)
        assertThat(utbetalingerFrontend.bekreftelse).isTrue
        assertThat(utbetalingerFrontend.annet).isTrue
        assertThat(utbetalingerFrontend.beskrivelseAvAnnet).isEqualTo(beskrivelse)
    }

    @Test
    fun putUtbetalingerSkalSetteAltFalseDersomManVelgerHarIkkeUtbetalinger() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithUtbetalinger(
            true, listOf(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING, UTBETALING_ANNET), "Lottogevinst"
        )

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val utbetalingerFrontend = UtbetalingerFrontend(bekreftelse = false)
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetalingBekreftelse = bekreftelser[0]
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalingBekreftelse.verdi).isFalse
        assertThat(utbetalinger).isEmpty()
    }

    @Test
    fun putUtbetalingerSkalSetteAlleBekreftelserLikFalse() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithUtbetalinger(
            true, listOf(UTBETALING_UTBYTTE, UTBETALING_SALG, UTBETALING_FORSIKRING, UTBETALING_ANNET), "Lottogevinst"
        )

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val utbetalingerFrontend = UtbetalingerFrontend(bekreftelse = false)
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetalingBekreftelse = bekreftelser[0]
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        val beskrivelse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.utbetaling
        assertThat(utbetalingBekreftelse.verdi).isFalse
        assertThat(utbetalinger).isEmpty()
        assertThat(beskrivelse).isBlank
    }

    @Test
    fun putUtbetalingerSkalSetteNoenBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val utbetalingerFrontend = UtbetalingerFrontend(
            bekreftelse = true,
            utbytte = false,
            salg = true,
            forsikring = true,
            annet = false
        )
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetalingBekreftelse = bekreftelser[0]
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalingBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetalingBekreftelse.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_UTBETALING)
        assertThat(utbetalingBekreftelse.verdi).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_FORSIKRING }).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_SALG }).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_UTBYTTE }).isFalse
        assertThat(utbetalinger.any { it.type == UTBETALING_ANNET }).isFalse
    }

    @Test
    fun putUtbetalingerSkalSetteAlleBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val utbetalingerFrontend = UtbetalingerFrontend(
            bekreftelse = true,
            utbytte = true,
            salg = true,
            forsikring = true,
            annet = true
        )
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetalingBekreftelse = bekreftelser[0]
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetalingBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetalingBekreftelse.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_UTBETALING)
        assertThat(utbetalingBekreftelse.verdi).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_FORSIKRING }).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_SALG }).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_UTBYTTE }).isTrue
        assertThat(utbetalinger.any { it.type == UTBETALING_ANNET }).isTrue
    }

    @Test
    fun putUtbetalingerSkalFjerneBeskrivelseAvAnnetDersomAnnetBlirAvkreftet() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithUtbetalinger(
            true, listOf(UTBETALING_ANNET), "Lottogevinst"
        )

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val utbetalingerFrontend = UtbetalingerFrontend(bekreftelse = false)
        utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend)

        val soknadUnderArbeid = slot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetalingBekreftelse = bekreftelser[0]
        val beskrivelse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet.utbetaling
        assertThat(utbetalingBekreftelse.verdi).isFalse
        assertThat(beskrivelse).isBlank
    }

    @Test
    fun getUtbetalingerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { utbetalingRessurs.hentUtbetalinger(BEHANDLINGSID) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putUtbetalingerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val utbetalingerFrontend = UtbetalingerFrontend()
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { utbetalingRessurs.updateUtbetalinger(BEHANDLINGSID, utbetalingerFrontend) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    private fun createJsonInternalSoknadWithUtbetalinger(
        harUtbetalinger: Boolean,
        utbetalingTyper: List<String>,
        beskrivelseAvAnnet: String?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            createEmptyJsonInternalSoknad(
                EIER
            )
        )
        val utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling> = ArrayList()
        for (utbetaling in utbetalingTyper) {
            utbetalinger.add(
                JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(utbetaling)
                    .withTittel("tittel")
            )
        }
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse = listOf(
            JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(SoknadJsonTyper.BEKREFTELSE_UTBETALING)
                .withVerdi(harUtbetalinger)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling = utbetalinger
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.beskrivelseAvAnnet =
            JsonOkonomibeskrivelserAvAnnet().withUtbetaling(beskrivelseAvAnnet)
        return soknadUnderArbeid
    }
}
