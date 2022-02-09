package no.nav.sosialhjelp.soknad.utgifter

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.BEKREFTELSE_BARNEUTGIFTER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_ANNET_BARN
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARNEHAGE
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_FRITIDSAKTIVITETER
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_BARN_TANNREGULERING
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTGIFTER_SFO
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.utgifter.BarneutgiftRessurs.BarneutgifterFrontend
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BarneutgiftRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val textService: TextService = mockk()
    private val barneutgiftRessurs = BarneutgiftRessurs(tilgangskontroll, soknadUnderArbeidRepository, textService)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
        clearAllMocks()
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        System.clearProperty("environment.name")
    }

    @Test
    fun barneutgifterSkalReturnereBekreftelseLikNullOgAltFalse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID)
        assertThat(barneutgifterFrontend.harForsorgerplikt).isFalse
        assertThat(barneutgifterFrontend.bekreftelse).isNull()
        assertThat(barneutgifterFrontend.barnehage).isFalse
        assertThat(barneutgifterFrontend.sfo).isFalse
        assertThat(barneutgifterFrontend.tannregulering).isFalse
        assertThat(barneutgifterFrontend.fritidsaktiviteter).isFalse
        assertThat(barneutgifterFrontend.annet).isFalse
    }

    @Test
    fun barneutgifterSkalReturnereHarForsorgerpliktLikFalseForPersonUtenBarn() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBarneutgifter(false, false, emptyList())

        val barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID)
        assertThat(barneutgifterFrontend.harForsorgerplikt).isFalse
        assertThat(barneutgifterFrontend.bekreftelse).isNull()
        assertThat(barneutgifterFrontend.barnehage).isFalse
        assertThat(barneutgifterFrontend.sfo).isFalse
        assertThat(barneutgifterFrontend.tannregulering).isFalse
        assertThat(barneutgifterFrontend.fritidsaktiviteter).isFalse
        assertThat(barneutgifterFrontend.annet).isFalse
    }

    @Test
    fun barneutgifterSkalReturnereBekreftelserLikTrue() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBarneutgifter(
                harForsorgerplikt = true,
                harUtgifter = true,
                utgiftstyper = listOf(
                    UTGIFTER_BARNEHAGE,
                    UTGIFTER_SFO,
                    UTGIFTER_BARN_FRITIDSAKTIVITETER,
                    UTGIFTER_BARN_TANNREGULERING,
                    UTGIFTER_ANNET_BARN
                )
            )

        val barneutgifterFrontend = barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID)
        assertThat(barneutgifterFrontend.harForsorgerplikt).isTrue
        assertThat(barneutgifterFrontend.bekreftelse).isTrue
        assertThat(barneutgifterFrontend.barnehage).isTrue
        assertThat(barneutgifterFrontend.sfo).isTrue
        assertThat(barneutgifterFrontend.tannregulering).isTrue
        assertThat(barneutgifterFrontend.fritidsaktiviteter).isTrue
        assertThat(barneutgifterFrontend.annet).isTrue
    }

    @Test
    fun putBarneutgifterSkalSetteAltFalseDersomManVelgerHarIkkeBarneutgifter() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBarneutgifter(
                true, true,
                listOf(
                    UTGIFTER_BARNEHAGE, UTGIFTER_SFO,
                    UTGIFTER_BARN_FRITIDSAKTIVITETER, UTGIFTER_ANNET_BARN
                )
            )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val barneutgifterFrontend = BarneutgifterFrontend(harForsorgerplikt = true, bekreftelse = false)
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data
            .okonomi.opplysninger.bekreftelse
        val barneutgiftBekreftelse = bekreftelser[0]
        val oversiktBarneutgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data
            .okonomi.oversikt.utgift
        val opplysningerBarneutgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data
            .okonomi.opplysninger.utgift
        assertThat(barneutgiftBekreftelse.verdi).isFalse
        assertThat(oversiktBarneutgifter.isEmpty()).isTrue
        assertThat(opplysningerBarneutgifter.isEmpty()).isTrue
    }

    @Test
    fun putBarneutgifterSkalSetteNoenBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val barneutgifterFrontend = BarneutgifterFrontend(
            harForsorgerplikt = true,
            bekreftelse = true,
            fritidsaktiviteter = false,
            barnehage = true,
            sfo = true,
            tannregulering = false,
            annet = false
        )
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val barneutgiftBekreftelse = bekreftelser[0]
        val oversiktBarneutgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.utgift
        val opplysningerBarneutgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utgift
        assertThat(barneutgiftBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(barneutgiftBekreftelse.type).isEqualTo(BEKREFTELSE_BARNEUTGIFTER)
        assertThat(barneutgiftBekreftelse.verdi).isTrue
        assertThat(oversiktBarneutgifter.any { it.type == UTGIFTER_BARNEHAGE }).isTrue
        assertThat(oversiktBarneutgifter.any { it.type == UTGIFTER_SFO }).isTrue
        assertThat(opplysningerBarneutgifter.any { it.type == UTGIFTER_BARN_FRITIDSAKTIVITETER }).isFalse
        assertThat(opplysningerBarneutgifter.any { it.type == UTGIFTER_BARN_TANNREGULERING }).isFalse
        assertThat(opplysningerBarneutgifter.any { it.type == UTGIFTER_ANNET_BARN }).isFalse
    }

    @Test
    fun putBarneutgifterSkalSetteAlleBekreftelser() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val barneutgifterFrontend = BarneutgifterFrontend(
            harForsorgerplikt = true,
            bekreftelse = true,
            fritidsaktiviteter = true,
            barnehage = true,
            sfo = true,
            tannregulering = true,
            annet = true
        )
        barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val bekreftelser = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        val barneutgiftBekreftelse = bekreftelser[0]
        val oversiktBarneutgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.utgift
        val opplysningerBarneutgifter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utgift
        assertThat(barneutgiftBekreftelse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(barneutgiftBekreftelse.type).isEqualTo(BEKREFTELSE_BARNEUTGIFTER)
        assertThat(barneutgiftBekreftelse.verdi).isTrue
        assertThat(oversiktBarneutgifter.any { it.type == UTGIFTER_BARNEHAGE }).isTrue
        assertThat(oversiktBarneutgifter.any { it.type == UTGIFTER_SFO }).isTrue
        assertThat(opplysningerBarneutgifter.any { it.type == UTGIFTER_BARN_FRITIDSAKTIVITETER }).isTrue
        assertThat(opplysningerBarneutgifter.any { it.type == UTGIFTER_BARN_TANNREGULERING }).isTrue
        assertThat(opplysningerBarneutgifter.any { it.type == UTGIFTER_ANNET_BARN }).isTrue
    }

    @Test
    fun barneutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { barneutgiftRessurs.hentBarneutgifter(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putBarneutgifterSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val barneutgifterFrontend = BarneutgifterFrontend()
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { barneutgiftRessurs.updateBarneutgifter(BEHANDLINGSID, barneutgifterFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun createJsonInternalSoknadWithBarneutgifter(
        harForsorgerplikt: Boolean,
        harUtgifter: Boolean,
        utgiftstyper: List<String>
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        val oversiktUtgifter: MutableList<JsonOkonomioversiktUtgift> = ArrayList()
        val opplysningUtgifter: MutableList<JsonOkonomiOpplysningUtgift> = ArrayList()
        for (utgiftstype in utgiftstyper) {
            if (utgiftstype == UTGIFTER_BARNEHAGE || utgiftstype == UTGIFTER_SFO) {
                oversiktUtgifter.add(
                    JsonOkonomioversiktUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel")
                )
            } else if (utgiftstype == UTGIFTER_BARN_FRITIDSAKTIVITETER || utgiftstype == UTGIFTER_BARN_TANNREGULERING || utgiftstype == UTGIFTER_ANNET_BARN) {
                opplysningUtgifter.add(
                    JsonOkonomiOpplysningUtgift()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(utgiftstype)
                        .withTittel("tittel")
                )
            }
        }
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse = listOf(
            JsonOkonomibekreftelse()
                .withKilde(JsonKilde.BRUKER)
                .withType(BEKREFTELSE_BARNEUTGIFTER)
                .withVerdi(harUtgifter)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.utgift = oversiktUtgifter
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utgift = opplysningUtgifter
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie.forsorgerplikt = JsonForsorgerplikt()
            .withHarForsorgerplikt(
                JsonHarForsorgerplikt()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(harForsorgerplikt)
            )
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
    }
}
