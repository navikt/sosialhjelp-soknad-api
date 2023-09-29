package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SkattbarInntektRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val skatteetatenSystemdata: SkatteetatenSystemdata = mockk()
    private val textService: TextService = mockk()
    private val skattbarInntektRessurs = SkattbarInntektRessurs(
        tilgangskontroll,
        soknadUnderArbeidRepository,
        skatteetatenSystemdata,
        textService
    )

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
    fun skattbarInntektSkalReturnereTomListe() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createSoknadUnderArbeid()

        val skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID)
        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten).isEmpty()
    }

    @Test
    fun skattbarInntektSkalReturnereBekreftetSkattbarInntekt() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithSkattbarInntekt(true)

        val skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID)
        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten).hasSize(1)
    }

    @Test
    fun skattbarInntektSkalReturnereHarIkkeSkattbarInntekt() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns createJsonInternalSoknadWithSkattbarInntekt(false)

        val skattbarInntektFrontend = skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID)
        assertThat(skattbarInntektFrontend.inntektFraSkatteetaten).isEmpty()
    }

    @Test
    fun skattbarInntekt_skalGiSamtykke() {
        val soknad = createJsonInternalSoknadWithSkattbarInntekt(false)
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknad
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val systemdataSlot = slot<SoknadUnderArbeid>()
        every { skatteetatenSystemdata.updateSystemdataIn(capture(systemdataSlot)) } just runs

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, true, "token")

        // Sjekker kaller til skatteetatenSystemdata
        verify { skatteetatenSystemdata.updateSystemdataIn(systemdataSlot.captured) }

        val okonomi = systemdataSlot.captured.jsonInternalSoknad!!.soknad.data.okonomi
        val fangetBekreftelse = okonomi.opplysninger.bekreftelse[0]
        assertThat(fangetBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(fangetBekreftelse.verdi).isTrue

        // Sjekker lagring av soknaden
        val spartSoknad = soknadUnderArbeidSlot.captured
        assertThat(spartSoknad.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse).hasSize(1)
        val spartBekreftelse = soknad.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.bekreftelse[0]
        assertThat(spartBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(spartBekreftelse.verdi).isTrue
    }

    @Test
    fun skattbarInntekt_skalTaBortSamtykke() {
        val soknad = createJsonInternalSoknadWithSkattbarInntekt(false)
        val opplysninger = soknad.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger
        OkonomiMapper.setBekreftelse(opplysninger, SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE, true, "")
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknad
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val systemdataSlot = slot<SoknadUnderArbeid>()
        every { skatteetatenSystemdata.updateSystemdataIn(capture(systemdataSlot)) } just runs

        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, false, "token")

        // Sjekker kaller til skattbarInntektSystemdata
        verify { skatteetatenSystemdata.updateSystemdataIn(systemdataSlot.captured) }

        val okonomi = systemdataSlot.captured.jsonInternalSoknad!!.soknad.data.okonomi
        val fangetBekreftelse = okonomi.opplysninger.bekreftelse[0]
        assertThat(fangetBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(fangetBekreftelse.verdi).isFalse

        // Sjekker lagring av soknaden
        val spartSoknad = soknadUnderArbeidSlot.captured
        val sparteOpplysninger = spartSoknad.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger
        assertThat(sparteOpplysninger.bekreftelse).hasSize(1)
        val spartBekreftelse = sparteOpplysninger.bekreftelse[0]
        assertThat(spartBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(spartBekreftelse.verdi).isFalse
    }

    @Test
    fun skattbarInntektkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { skattbarInntektRessurs.hentSkattbareInntekter(BEHANDLINGSID) }

        verify(exactly = 0) { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) }
    }

    @Test
    fun putSamtykkeSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(BEHANDLINGSID) } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, true, "token") }

        verify(exactly = 0) { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) }
    }

    private fun createJsonInternalSoknadWithSkattbarInntekt(harSkattbarInntekt: Boolean): SoknadUnderArbeid {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        if (harSkattbarInntekt) {
            val utbetaling = JsonOkonomiOpplysningUtbetaling()
                .withType(SoknadJsonTyper.UTBETALING_SKATTEETATEN)
                .withKilde(JsonKilde.SYSTEM)
                .withTittel("Utbetalingen!")
                .withBelop(123456)
            soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.okonomi.opplysninger.utbetaling.add(utbetaling)
        }
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"

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
