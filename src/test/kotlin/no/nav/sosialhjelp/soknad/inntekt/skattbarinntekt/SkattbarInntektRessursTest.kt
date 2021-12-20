package no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.TextService
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.common.mapper.OkonomiMapper
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SkattbarInntektRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val skatteetatenSystemdata: SkatteetatenSystemdata = mockk()
    private val textService: TextService = mockk()
    private val skattbarInntektRessurs =
        SkattbarInntektRessurs(tilgangskontroll, soknadUnderArbeidRepository, skatteetatenSystemdata, textService)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    fun skattbarInntektSkalReturnereTomListe() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns SoknadUnderArbeid().withJsonInternalSoknad(SoknadService.createEmptyJsonInternalSoknad(EIER))

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
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns createJsonInternalSoknadWithSkattbarInntekt(false)

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

        val okonomi = systemdataSlot.captured.jsonInternalSoknad.soknad.data.okonomi
        val fangetBekreftelse = okonomi.opplysninger.bekreftelse[0]
        assertThat(fangetBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(fangetBekreftelse.verdi).isTrue

        // Sjekker lagring av soknaden
        val spartSoknad = soknadUnderArbeidSlot.captured
        assertThat(spartSoknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse).hasSize(1)
        val spartBekreftelse = soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse[0]
        assertThat(spartBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(spartBekreftelse.verdi).isTrue
    }

    @Test
    fun skattbarInntekt_skalTaBortSamtykke() {
        val soknad = createJsonInternalSoknadWithSkattbarInntekt(false)
        val opplysninger = soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger
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

        val okonomi = systemdataSlot.captured.jsonInternalSoknad.soknad.data.okonomi
        val fangetBekreftelse = okonomi.opplysninger.bekreftelse[0]
        assertThat(fangetBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(fangetBekreftelse.verdi).isFalse

        // Sjekker lagring av soknaden
        val spartSoknad = soknadUnderArbeidSlot.captured
        val sparteOpplysninger = spartSoknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger
        assertThat(sparteOpplysninger.bekreftelse).hasSize(1)
        val spartBekreftelse = sparteOpplysninger.bekreftelse[0]
        assertThat(spartBekreftelse.type).isEqualTo(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
        assertThat(spartBekreftelse.verdi).isFalse
    }

    @Test
    fun skattbarInntekt_skalIkkeForandreSamtykke() {
        val soknad = createJsonInternalSoknadWithSkattbarInntekt(false)
        every {
            soknadUnderArbeidRepository.hentSoknad(any<String>(), any())
        } returns soknad
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        skattbarInntektRessurs.updateSamtykke(BEHANDLINGSID, false, "token")

        // Sjekker kaller til skattbarInntektSystemdata
        verify(exactly = 0) { skatteetatenSystemdata.updateSystemdataIn(any()) }

        // Sjekker lagring av soknaden
        verify(exactly = 0) { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) }

        // Sjekker soknaden
        assertThat(soknad.jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse).isEmpty()
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
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(SoknadService.createEmptyJsonInternalSoknad(EIER))
        if (harSkattbarInntekt) {
            val utbetaling = JsonOkonomiOpplysningUtbetaling()
                .withType(SoknadJsonTyper.UTBETALING_SKATTEETATEN)
                .withKilde(JsonKilde.SYSTEM)
                .withTittel("Utbetalingen!")
                .withBelop(123456)
            soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling.add(utbetaling)
        }
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
    }
}
