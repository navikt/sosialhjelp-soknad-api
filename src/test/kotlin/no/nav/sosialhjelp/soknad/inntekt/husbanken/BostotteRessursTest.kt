package no.nav.sosialhjelp.soknad.inntekt.husbanken

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.husbanken.BostotteRessurs.BostotteFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BostotteRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val bostotteSystemdata: BostotteSystemdata = mockk()
    private val textService: TextService = mockk()
    private val bostotteRessurs =
        BostotteRessurs(tilgangskontroll, soknadUnderArbeidRepository, bostotteSystemdata, textService)

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
    fun bostotteSkalReturnereNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val (bekreftelse) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(bekreftelse).isNull()
    }

    @Test
    fun bostotteSkalReturnereBekreftetBostotte() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithBostotte(true)
        val (bekreftelse) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(bekreftelse).isTrue
    }

    @Test
    fun bostotteSkalReturnereHarIkkeBostotte() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithBostotte(false)
        val (bekreftelse) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(bekreftelse).isFalse
    }

    @Test
    fun putBostotteSkalSetteBostotteOgLeggeTilInntektstypen() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val argument = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(argument), any()) } just runs

        val bostotteFrontend = BostotteFrontend(true, null, null, null, null, null)
        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend, "token")

        val soknadUnderArbeid = argument.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetaling = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetaling[0].type).isEqualTo(SoknadJsonTyper.UTBETALING_HUSBANKEN)

        val bostotte = bekreftelser[0]
        assertThat(bostotte.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(bostotte.type).isEqualTo(SoknadJsonTyper.BOSTOTTE)
        assertThat(bostotte.verdi).isTrue
    }

    @Test
    fun putBostotteSkalSetteHarIkkeBostotteOgSletteInntektstypen() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val soknad = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val inntekt = ArrayList<JsonOkonomioversiktInntekt>()
        inntekt.add(JsonOkonomioversiktInntekt().withType(SoknadJsonTyper.BOSTOTTE))
        soknad.jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt = inntekt

        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns soknad
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
        val argument = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(argument), any()) } just runs

        val bostotteFrontend = BostotteFrontend(false, null, null, null, null, null)
        bostotteRessurs.updateBostotte(BEHANDLINGSID, bostotteFrontend, "token")

        val soknadUnderArbeid = argument.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val utbetaling = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        assertThat(utbetaling).isEmpty()

        val bostotte = bekreftelser[0]
        assertThat(bostotte.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(bostotte.type).isEqualTo(SoknadJsonTyper.BOSTOTTE)
        assertThat(bostotte.verdi).isFalse
    }

    @Test
    fun bostotte_skalBareHaUtRiktigUtbetaling() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithBostotteUtbetalinger(true, listOf("tilfeldig", "salg", "lonn"))

        val (_, _, utbetalinger) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(utbetalinger).hasSize(1)
    }

    @Test
    fun bostotte_skalIkkeHaUtbetaling() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithBostotteUtbetalinger(false, listOf("tilfeldig", "salg", "lonn"))
        val (_, _, utbetalinger) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(utbetalinger).isEmpty()
    }

    @Test
    fun bostotte_skalBareHaUtRiktigSak() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithSaker(true, listOf("tilfeldig", "salg", "lonn"))
        val (_, _, _, saker) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(saker).hasSize(1)
    }

    @Test
    fun bostotte_skalIkkeHaSak() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithSaker(false, listOf("tilfeldig", "salg", "lonn"))
        val (_, _, _, saker) = bostotteRessurs.hentBostotte(BEHANDLINGSID)
        assertThat(saker).isEmpty()
    }

    @Test
    fun bostotte_skalGiSamtykke() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val soknad = createJsonInternalSoknadWithSaker(false, listOf("tilfeldig", "salg", "lonn"))
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns soknad
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val argument = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(argument), any()) } just runs

        val systemdataSlot = slot<SoknadUnderArbeid>()
        every { bostotteSystemdata.updateSystemdataIn(capture(systemdataSlot), any()) } just runs

        bostotteRessurs.updateSamtykke(BEHANDLINGSID, true, "token")

        val okonomi = systemdataSlot.captured.jsonInternalSoknad.soknad.data.okonomi
        val fangetBekreftelse = okonomi.opplysninger.bekreftelse[0]
        assertThat(fangetBekreftelse.type).isEqualTo(SoknadJsonTyper.BOSTOTTE_SAMTYKKE)
        assertThat(fangetBekreftelse.verdi).isTrue

        // Sjekker lagring av soknaden
        val spartSoknad = argument.captured
        assertThat(spartSoknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse).hasSize(1)
        val spartBekreftelse = soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse[0]
        assertThat(spartBekreftelse.type).isEqualTo(SoknadJsonTyper.BOSTOTTE_SAMTYKKE)
        assertThat(spartBekreftelse.verdi).isTrue
    }

    @Test
    fun bostotte_skalTaBortSamtykke() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val soknad = createJsonInternalSoknadWithSaker(false, listOf("tilfeldig", "salg", "lonn"))
        val opplysninger = soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger
        OkonomiMapper.setBekreftelse(opplysninger, SoknadJsonTyper.BOSTOTTE_SAMTYKKE, true, "")
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns soknad
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        val argument = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(argument), any()) } just runs

        val systemdataSlot = slot<SoknadUnderArbeid>()
        every { bostotteSystemdata.updateSystemdataIn(capture(systemdataSlot), any()) } just runs

        bostotteRessurs.updateSamtykke(BEHANDLINGSID, false, "token")

        // Sjekker kaller til bostotteSystemdata
        val okonomi = systemdataSlot.captured.jsonInternalSoknad.soknad.data.okonomi
        val fangetBekreftelse = okonomi.opplysninger.bekreftelse[0]
        assertThat(fangetBekreftelse.type).isEqualTo(SoknadJsonTyper.BOSTOTTE_SAMTYKKE)
        assertThat(fangetBekreftelse.verdi).isFalse

        // Sjekker lagring av soknaden
        val spartSoknad = argument.captured
        val sparteOpplysninger = spartSoknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger
        assertThat(sparteOpplysninger.bekreftelse).hasSize(1)
        val spartBekreftelse = sparteOpplysninger.bekreftelse[0]
        assertThat(spartBekreftelse.type).isEqualTo(SoknadJsonTyper.BOSTOTTE_SAMTYKKE)
        assertThat(spartBekreftelse.verdi).isFalse
    }

    @Test
    fun bostotte_skalIkkeForandreSamtykke() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val soknad = createJsonInternalSoknadWithSaker(false, listOf("tilfeldig", "salg", "lonn"))
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns soknad
        bostotteRessurs.updateSamtykke(BEHANDLINGSID, false, "token")

        // Sjekker kaller til bostotteSystemdata
        verify(exactly = 0) { bostotteSystemdata.updateSystemdataIn(any(), any()) }

        // Sjekker lagring av soknaden
        verify(exactly = 0) { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) }

        // Sjekker soknaden
        assertThat(soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse).isEmpty()
    }

    @Test
    fun bostotteSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { bostotteRessurs.hentBostotte(BEHANDLINGSID) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putBostotteSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")
        val bostotteFrontend = BostotteFrontend(true, null, null, null, null, null)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy {
                bostotteRessurs.updateBostotte(
                    BEHANDLINGSID,
                    bostotteFrontend,
                    "token"
                )
            }
        verify(exactly = 0) { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) }
    }

    @Test
    fun putSamtykkeSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID) } throws AuthorizationException("Not for you my friend")
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy {
                bostotteRessurs.updateSamtykke(
                    BEHANDLINGSID,
                    true,
                    "token"
                )
            }
        verify(exactly = 0) { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) }
    }

    private fun catchSoknadUnderArbeidSentToOppdaterSoknadsdata(): SoknadUnderArbeid {
        val argument = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(argument), any()) } just runs
        return argument.captured
    }

    private fun createJsonInternalSoknadWithBostotte(verdi: Boolean): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.withBekreftelse(
            listOf(
                JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(SoknadJsonTyper.BOSTOTTE)
                    .withVerdi(verdi)
            )
        )
        return soknadUnderArbeid
    }

    private fun createJsonInternalSoknadWithBostotteUtbetalinger(
        harUtbetalinger: Boolean,
        utbetalingTyper: List<String>
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
                    .withKilde(JsonKilde.SYSTEM)
                    .withType(utbetaling)
                    .withTittel("tittel")
            )
        }
        if (harUtbetalinger) {
            utbetalinger.add(
                JsonOkonomiOpplysningUtbetaling()
                    .withKilde(JsonKilde.SYSTEM)
                    .withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)
                    .withTittel("tittel")
            )
        }
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling = utbetalinger
        return soknadUnderArbeid
    }

    private fun createJsonInternalSoknadWithSaker(harSaker: Boolean, saksTyper: List<String>): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            createEmptyJsonInternalSoknad(EIER)
        )
        val saker: MutableList<JsonBostotteSak> = ArrayList()
        for (sak in saksTyper) {
            saker.add(
                JsonBostotteSak()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withType(sak)
                    .withStatus("STATUS")
            )
        }
        if (harSaker) {
            saker.add(
                JsonBostotteSak()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)
                    .withStatus("UNDER_BEHANDLING")
            )
        }
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bostotte.saker = saker
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
    }
}
