package no.nav.sosialhjelp.soknad.utgifter

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
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BOSTOTTE_SAMTYKKE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BO
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_AVDRAG
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BOLIGLAN_RENTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_HUSLEIE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_KOMMUNAL_AVGIFT
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_OPPVARMING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_STROM
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper.setBekreftelse
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.utgifter.BoutgiftRessurs.BoutgifterFrontend
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class BoutgiftRessursTest {
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()

    private val boutgiftRessurs = BoutgiftRessurs(tilgangskontroll, soknadUnderArbeidRepository, textService)

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
    fun boutgifterSkalReturnereBekreftelseLikNullOgAlleUnderverdierLikFalse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid()

        val boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID)
        assertThat(boutgifterFrontend.bekreftelse).isNull()
        assertThat(boutgifterFrontend.husleie).isFalse
        assertThat(boutgifterFrontend.strom).isFalse
        assertThat(boutgifterFrontend.oppvarming).isFalse
        assertThat(boutgifterFrontend.kommunalAvgift).isFalse
        assertThat(boutgifterFrontend.boliglan).isFalse
        assertThat(boutgifterFrontend.annet).isFalse
    }

    @Test
    fun boutgifterSkalReturnereBekreftelserLikTrue() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBoutgifter(
                true,
                listOf(
                    UTGIFTER_HUSLEIE,
                    UTGIFTER_STROM,
                    UTGIFTER_KOMMUNAL_AVGIFT,
                    UTGIFTER_OPPVARMING,
                    UTGIFTER_BOLIGLAN_AVDRAG,
                    UTGIFTER_BOLIGLAN_RENTER,
                    UTGIFTER_ANNET_BO,
                ),
            )

        val boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID)
        assertThat(boutgifterFrontend.bekreftelse).isTrue
        assertThat(boutgifterFrontend.husleie).isTrue
        assertThat(boutgifterFrontend.strom).isTrue
        assertThat(boutgifterFrontend.oppvarming).isTrue
        assertThat(boutgifterFrontend.kommunalAvgift).isTrue
        assertThat(boutgifterFrontend.boliglan).isTrue
        assertThat(boutgifterFrontend.annet).isTrue
    }

    @Test
    fun boutgifterSkalReturnereSkalViseInfoLikTrueDersomManHverkenHarBostotteSakerEllerUtbetalinger() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid()

        val boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID)
        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isTrue
    }

    @Test
    fun boutgifterSkalReturnereSkalViseInfoLikFalseDersomManHarBostotteSakerEllerUtbetalinger() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bostotte =
            JsonBostotte()
                .withSaker(listOf(JsonBostotteSak().withType(SoknadJsonTyper.UTBETALING_HUSBANKEN)))
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling =
            listOf(JsonOkonomiOpplysningUtbetaling().withType(SoknadJsonTyper.UTBETALING_HUSBANKEN))
        setBekreftelse(
            soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger,
            BOSTOTTE_SAMTYKKE,
            true,
            "Test samtykke!",
        )
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        val boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID)
        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isFalse
    }

    @Test
    fun boutgifterSkalReturnereSkalViseInfoLikTrueDersomHusbankenErNedeOgManSvarerNeiTilBostotte() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.driftsinformasjon.stotteFraHusbankenFeilet = true
        setBekreftelse(
            soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger,
            BOSTOTTE_SAMTYKKE,
            true,
            "Test samtykke!",
        )
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse =
            listOf(JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE).withVerdi(false))
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        val boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID)
        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isTrue
    }

    @Test
    fun boutgifterSkalReturnereSkalViseInfoLikTrueDersomViMAnglerSamtykkeOgManSvarerNeiTilBostotte() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse =
            listOf(
                JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE).withVerdi(false),
                JsonOkonomibekreftelse().withKilde(JsonKilde.BRUKER).withType(BOSTOTTE_SAMTYKKE).withVerdi(false),
            )
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        val boutgifterFrontend = boutgiftRessurs.hentBoutgifter(BEHANDLINGSID)
        assertThat(boutgifterFrontend.skalViseInfoVedBekreftelse).isTrue
    }

    @Test
    fun putBoutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBoutgifter() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBoutgifter(
                true,
                listOf(UTGIFTER_HUSLEIE, UTGIFTER_STROM, UTGIFTER_KOMMUNAL_AVGIFT, UTGIFTER_ANNET_BO),
            )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val boutgifterFrontend = BoutgifterFrontend(bekreftelse = false)
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse
        val boutgiftBekreftelse = bekreftelser[0]
        val oversiktBoutgifter = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.oversikt.utgift
        val opplysningerBoutgifter = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utgift
        assertThat(boutgiftBekreftelse.verdi).isFalse
        assertThat(oversiktBoutgifter.isEmpty()).isTrue
        assertThat(opplysningerBoutgifter.isEmpty()).isTrue
    }

    @Test
    fun putBoutgifterSkalSetteNoenBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid()

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val boutgifterFrontend =
            BoutgifterFrontend(
                bekreftelse = true,
                husleie = true,
                strom = true,
                kommunalAvgift = false,
                oppvarming = false,
                boliglan = false,
                annet = false,
            )
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse
        val boutgiftBekreftelse = bekreftelser[0]
        val oversiktBoutgifter = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.oversikt.utgift
        val opplysningerBoutgifter = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utgift
        assertThat(boutgiftBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(boutgiftBekreftelse.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER)
        assertThat(boutgiftBekreftelse.verdi).isTrue
        assertThat(oversiktBoutgifter.any { it.type == UTGIFTER_HUSLEIE }).isTrue
        assertThat(oversiktBoutgifter.any { it.type == UTGIFTER_BOLIGLAN_AVDRAG }).isFalse
        assertThat(oversiktBoutgifter.any { it.type == UTGIFTER_BOLIGLAN_RENTER }).isFalse
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_STROM }).isTrue
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_KOMMUNAL_AVGIFT }).isFalse
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_OPPVARMING }).isFalse
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_ANNET_BO }).isFalse
    }

    @Test
    fun putBoutgifterSkalSetteAlleBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid()

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val boutgifterFrontend =
            BoutgifterFrontend(
                bekreftelse = true,
                husleie = true,
                strom = true,
                kommunalAvgift = true,
                oppvarming = true,
                boliglan = true,
                annet = true,
            )
        boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse
        val boutgiftBekreftelse = bekreftelser[0]
        val oversiktBoutgifter = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.oversikt.utgift
        val opplysningerBoutgifter = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utgift
        assertThat(boutgiftBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(boutgiftBekreftelse.type).isEqualTo(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER)
        assertThat(boutgiftBekreftelse.verdi).isTrue
        assertThat(oversiktBoutgifter.any { it.type == UTGIFTER_HUSLEIE }).isTrue
        assertThat(oversiktBoutgifter.any { it.type == UTGIFTER_BOLIGLAN_AVDRAG }).isTrue
        assertThat(oversiktBoutgifter.any { it.type == UTGIFTER_BOLIGLAN_RENTER }).isTrue
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_STROM }).isTrue
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_KOMMUNAL_AVGIFT }).isTrue
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_OPPVARMING }).isTrue
        assertThat(opplysningerBoutgifter.any { it.type == UTGIFTER_ANNET_BO }).isTrue
    }

    @Test
    fun boutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { boutgiftRessurs.hentBoutgifter(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putBoutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val boutgifterFrontend = BoutgifterFrontend(null)
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { boutgiftRessurs.updateBoutgifter(BEHANDLINGSID, boutgifterFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun createJsonInternalSoknadWithBoutgifter(
        harUtgifter: Boolean,
        utgiftstyper: List<String>,
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val oversiktUtgifter: MutableList<JsonOkonomioversiktUtgift> = ArrayList()
        val opplysningUtgifter: MutableList<JsonOkonomiOpplysningUtgift> = ArrayList()
        for (utgiftstype in utgiftstyper) {
            if (utgiftstype == UTGIFTER_HUSLEIE || utgiftstype == UTGIFTER_BOLIGLAN_AVDRAG || utgiftstype == UTGIFTER_BOLIGLAN_RENTER) {
                oversiktUtgifter.add(
                    JsonOkonomioversiktUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"),
                )
            } else if (
                utgiftstype == UTGIFTER_STROM ||
                utgiftstype == UTGIFTER_OPPVARMING ||
                utgiftstype == UTGIFTER_KOMMUNAL_AVGIFT ||
                utgiftstype == UTGIFTER_ANNET_BO
            ) {
                opplysningUtgifter.add(
                    JsonOkonomiOpplysningUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel"),
                )
            }
        }
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse =
            listOf(
                JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.BRUKER)
                    .withType(SoknadJsonTyper.BEKREFTELSE_BOUTGIFTER)
                    .withVerdi(harUtgifter),
            )
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.oversikt.utgift = oversiktUtgifter
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utgift = opplysningUtgifter
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = BEHANDLINGSID,
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )
        }
    }
}
