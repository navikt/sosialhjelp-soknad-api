package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import jakarta.validation.Validation
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerRessurs.KontonummerInputDTO
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class KontonummerRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val kontonummerService: KontonummerService = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val kontonummerRessurs = KontonummerRessurs(tilgangskontroll, soknadUnderArbeidRepository, kontonummerService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { kontonummerService.getKontonummer(any()) } returns KONTONUMMER_SYSTEM
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun kontonummerSkalReturnereSystemKontonummer() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithKontonummer(JsonKilde.SYSTEM, KONTONUMMER_SYSTEM)

        val kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID)
        assertThat(kontonummerFrontend.brukerutfyltVerdi).isNull()
        assertThat(kontonummerFrontend.systemverdi).isEqualTo(KONTONUMMER_SYSTEM)
        assertThat(kontonummerFrontend.harIkkeKonto).isFalse()
    }

    @Test
    fun kontonummerSkalReturnereBrukerutfyltKontonummer() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, KONTONUMMER_BRUKER)

        val kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID)
        assertThat(kontonummerFrontend.brukerutfyltVerdi).isEqualTo(KONTONUMMER_BRUKER)
        assertThat(kontonummerFrontend.systemverdi).isEqualTo(KONTONUMMER_SYSTEM)
        assertThat(kontonummerFrontend.harIkkeKonto).isFalse()
    }

    @Test
    fun kontonummerSkalReturnereKontonummerLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, null)
        every { kontonummerService.getKontonummer(any()) } returns null

        val kontonummerFrontend = kontonummerRessurs.hentKontonummer(BEHANDLINGSID)
        assertThat(kontonummerFrontend.brukerutfyltVerdi).isNull()
        assertThat(kontonummerFrontend.systemverdi).isNull()
        assertThat(kontonummerFrontend.harIkkeKonto).isFalse()
    }

    @Test
    fun UgyldigKontonummerValidererIkke() {
        val invalidInput = KontonummerInputDTO(brukerutfyltVerdi = "invalid; should match ^\\d{11}$")
        println(Validation.buildDefaultValidatorFactory().validator.validate(invalidInput).map { it.message })
        val isValid = Validation.buildDefaultValidatorFactory().validator.validate(invalidInput).isEmpty()
        assertThat(isValid).isFalse()
    }

    @Test
    fun putKontonummerSkalSetteBrukerutfyltKontonummer() {
        startWithEmptyKontonummerAndNoSystemKontonummer()
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val kontonummerFrontend = KontonummerInputDTO(brukerutfyltVerdi = KONTONUMMER_BRUKER)
        kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend)

        val soknadUnderArbeid = slot.captured
        val kontonummer = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.kontonummer
        assertThat(kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
        // todo kontonummer.harIkkeKonto er null, men assertion gir NPE?
//        assertThat(kontonummer.harIkkeKonto).isNull()
        assertThat(kontonummer.verdi).isEqualTo(KONTONUMMER_BRUKER)
    }

    @Test
    fun putKontonummerSkalOverskriveBrukerutfyltKontonummerMedSystemKontonummer() {
        startWithBrukerKontonummerAndSystemKontonummerInTPS()
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val kontonummerFrontend = KontonummerInputDTO(brukerutfyltVerdi = null, harIkkeKonto = false)
        kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend)

        val soknadUnderArbeid = slot.captured
        val kontonummer = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.kontonummer
        assertThat(kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        // todo kontonummer.harIkkeKonto er null, men assertion gir NPE?
//        assertThat(kontonummer.harIkkeKonto).isNull()
        assertThat(kontonummer.verdi).isEqualTo(KONTONUMMER_SYSTEM)
    }

    @Test
    fun kontonummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { kontonummerRessurs.hentKontonummer(BEHANDLINGSID) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    @Test
    fun putKontonummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val kontonummerFrontend = KontonummerInputDTO()
        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { kontonummerRessurs.updateKontonummer(BEHANDLINGSID, kontonummerFrontend) }

        verify(exactly = 0) { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) }
    }

    private fun startWithBrukerKontonummerAndSystemKontonummerInTPS() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithKontonummer(JsonKilde.BRUKER, KONTONUMMER_BRUKER)
    }

    private fun startWithEmptyKontonummerAndNoSystemKontonummer() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithKontonummer(JsonKilde.SYSTEM, null)
    }

    private fun createJsonInternalSoknadWithKontonummer(kilde: JsonKilde, verdi: String?): SoknadUnderArbeid {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.kontonummer
            .withKilde(kilde)
            .withVerdi(verdi)
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
        private const val KONTONUMMER_BRUKER = "11122233344"
        private const val KONTONUMMER_SYSTEM = "44333222111"

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = BEHANDLINGSID,
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now(),
            )
        }
    }
}
