package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.mockk.Called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.TelefonnummerRessurs.TelefonnummerFrontend
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class TelefonnummerRessursTest {
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val telefonnummerSystemdata: TelefonnummerSystemdata = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val telefonnummerRessurs =
        TelefonnummerRessurs(tilgangskontroll, telefonnummerSystemdata, soknadUnderArbeidRepository)

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
    fun telefonnummerSkalReturnereSystemTelefonnummer() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithTelefonnummer(JsonKilde.SYSTEM, TELEFONNUMMER_SYSTEM)

        val telefonnummerFrontend: TelefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID)

        assertThat(telefonnummerFrontend.brukerutfyltVerdi).isNull()
        assertThat(telefonnummerFrontend.systemverdi).isEqualTo(TELEFONNUMMER_SYSTEM)
        assertThat(telefonnummerFrontend.brukerdefinert).isFalse
    }

    @Test
    fun telefonnummerSkalReturnereBrukerdefinertNaarTelefonnummerErLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithTelefonnummer(null, null)
        every { telefonnummerSystemdata.innhentSystemverdiTelefonnummer(any()) } returns null

        val telefonnummerFrontend: TelefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID)

        assertThat(telefonnummerFrontend.brukerutfyltVerdi).isNull()
        assertThat(telefonnummerFrontend.systemverdi).isNull()
        assertThat(telefonnummerFrontend.brukerdefinert).isTrue
    }

    @Test
    fun telefonnummerSkalReturnereBrukerutfyltTelefonnummer() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER)
        every { telefonnummerSystemdata.innhentSystemverdiTelefonnummer(any()) } returns TELEFONNUMMER_SYSTEM

        val telefonnummerFrontend = telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID)

        assertThat(telefonnummerFrontend.brukerutfyltVerdi).isEqualTo(TELEFONNUMMER_BRUKER)
        assertThat(telefonnummerFrontend.systemverdi).isEqualTo(TELEFONNUMMER_SYSTEM)
        assertThat(telefonnummerFrontend.brukerdefinert).isTrue
    }

    @Test
    fun putTelefonnummerSkalLageNyJsonTelefonnummerDersomDenVarNull() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithTelefonnummer(null, null)
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val telefonnummerFrontend = TelefonnummerFrontend(
            brukerdefinert = true,
            brukerutfyltVerdi = TELEFONNUMMER_BRUKER
        )
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val telefonnummer = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.telefonnummer
        assertThat(telefonnummer.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(telefonnummer.verdi).isEqualTo(TELEFONNUMMER_BRUKER)
    }

    @Test
    fun putTelefonnummerSkalOppdatereBrukerutfyltTelefonnummer() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithTelefonnummer(null, null)
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val telefonnummerFrontend = TelefonnummerFrontend(
            brukerdefinert = true,
            brukerutfyltVerdi = TELEFONNUMMER_BRUKER
        )
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val telefonnummer = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.telefonnummer
        assertThat(telefonnummer.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(telefonnummer.verdi).isEqualTo(TELEFONNUMMER_BRUKER)
    }

    @Test
    fun putTelefonnummerSkalOverskriveBrukerutfyltTelefonnummerMedSystemTelefonnummer() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithTelefonnummer(JsonKilde.BRUKER, TELEFONNUMMER_BRUKER)
        every { telefonnummerSystemdata.innhentSystemverdiTelefonnummer(any()) } returns TELEFONNUMMER_SYSTEM
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { telefonnummerSystemdata.updateSystemdataIn(any()) } answers { callOriginal() }

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val telefonnummerFrontend = TelefonnummerFrontend(brukerdefinert = false)
        telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val telefonnummer = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia.telefonnummer
        assertThat(telefonnummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(telefonnummer.verdi).isEqualTo(TELEFONNUMMER_SYSTEM)
    }

    @Test
    fun telefonnummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { telefonnummerRessurs.hentTelefonnummer(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    @Test
    fun putTelefonnummerSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val telefonnummerFrontend = TelefonnummerFrontend()

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { telefonnummerRessurs.updateTelefonnummer(BEHANDLINGSID, telefonnummerFrontend) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    private fun createJsonInternalSoknadWithTelefonnummer(kilde: JsonKilde?, verdi: String?): SoknadUnderArbeid {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
            .withTelefonnummer(
                verdi?.let {
                    JsonTelefonnummer()
                        .withKilde(kilde)
                        .withVerdi(it)
                }
            )
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
        private const val TELEFONNUMMER_BRUKER = "98765432"
        private const val TELEFONNUMMER_SYSTEM = "23456789"

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = BEHANDLINGSID,
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
