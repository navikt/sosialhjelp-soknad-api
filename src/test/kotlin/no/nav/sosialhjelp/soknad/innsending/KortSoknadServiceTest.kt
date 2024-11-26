package no.nav.sosialhjelp.soknad.innsending

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonDigisosSoker
import no.nav.sbl.soknadsosialhjelp.digisos.soker.JsonHendelse
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonSoknadsStatus
import no.nav.sbl.soknadsosialhjelp.digisos.soker.hendelse.JsonUtbetaling
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.DigisosSoker
import no.nav.sosialhjelp.soknad.innsending.SoknadServiceOld.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.humanifyHvaSokesOm
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class KortSoknadServiceTest {
    private val digisosApiService: DigisosApiService = mockk()
    private val soknadService: SoknadService = mockk()
    private val clock: Clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.of("UTC"))
    private val kortSoknadService: KortSoknadService =
        KortSoknadService(digisosApiService, clock, soknadService, mockk(), mockk(), mockk(relaxed = true))

    @Test
    fun `should qualify if there is a recent soknad from fiks`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createMottattHendelse("2022-10-01T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertTrue(result)
    }

    @Test
    fun `should not qualify if there are no recent soknad from fiks`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createMottattHendelse("2020-10-01T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertFalse(result)
    }

    @Test
    fun `should qualify if there is a recent utbetaling`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createPastUtbetaling("2022-10-01T00:00:00Z", "2022-12-24T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertTrue(result)
    }

    @Test
    fun `should not qualify if there is no recent utbetaling`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createPastUtbetaling("2022-10-01T00:00:00Z", "2022-08-01T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertFalse(result)
    }

    @Test
    fun `should qualify if there is an upcoming utbetaling`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createUpcomingUtbetaling("2022-10-01T00:00:00Z", "2023-01-10T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertTrue(result)
    }

    @Test
    fun `should not qualify if there is an upcoming utbetaling more than 14 days in the future`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createUpcomingUtbetaling("2022-10-01T00:00:00Z", "2023-02-01T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertFalse(result)
    }

    @Test
    fun `should not qualify if there is an upcoming utbetaling before today`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createUpcomingUtbetaling("2022-10-01T00:00:00Z", "2022-06-01T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertFalse(result)
    }

    @Test
    fun `should not qualify if there are no recent soknader or utbetalinger`() {
        every { digisosApiService.getSoknaderForUser(any()) } returns emptyList()
        every { digisosApiService.getInnsynsfilForSoknad(any(), any(), any()) } returns mockk()
        val result = kortSoknadService.isQualifiedFromFiks("12345678901", "token")

        assertFalse(result)
    }

    @Test
    fun `should short circuit if it finds a qualifying soknad`() {
        val digisosSak = createDigisosSak(sistEndret = 3L)
        val digisosSak2 = createDigisosSak(fiksDigisosId = "625986e9-1fbc-482c-acd5-a702059a6fba", sistEndret = 2L)
        val digisosSak3 = createDigisosSak(fiksDigisosId = "9edaffec-5065-4819-9173-b0d5ae39063f", sistEndret = 1L)
        val digisosSoker = createJsonDigisosSoker(listOf(createMottattHendelse("2022-10-01T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
                digisosSak2,
                digisosSak3,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertTrue(result)
        verify(exactly = 1) { digisosApiService.getInnsynsfilForSoknad(any(), any(), any()) }
    }

    @Test
    fun `should short circuit if it finds a qualifying utbetaling`() {
        val digisosSak = createDigisosSak(sistEndret = 3L)
        val digisosSak2 = createDigisosSak(fiksDigisosId = "625986e9-1fbc-482c-acd5-a702059a6fba", sistEndret = 2L)
        val digisosSak3 = createDigisosSak(fiksDigisosId = "9edaffec-5065-4819-9173-b0d5ae39063f", sistEndret = 1L)
        val digisosSoker = createJsonDigisosSoker(listOf(createUpcomingUtbetaling("2022-10-01T00:00:00Z", "2023-01-10T00:00:00Z")))
        every { digisosApiService.getSoknaderForUser(any()) } returns
            listOf(
                digisosSak,
                digisosSak2,
                digisosSak3,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "", "token") } returns digisosSoker

        val result = kortSoknadService.isQualifiedFromFiks("token", "0301")

        assertTrue(result)
        verify(exactly = 1) { digisosApiService.getInnsynsfilForSoknad(any(), any(), any()) }
    }

    @Test
    fun `Humanify skal returnere tom streng hvis ingen kategorier`() {
        val jsonInternalSoknad =
            createEmptyJsonInternalSoknad("12345678901", true)
                .apply { soknad.data.begrunnelse.hvaSokesOm = "[]" }

        jsonInternalSoknad.humanifyHvaSokesOm()

        assertThat(jsonInternalSoknad.soknad.data.begrunnelse.hvaSokesOm).isEqualTo("")
    }

    @Test
    fun `Hva sokes om er vanlig tekst`() {
        val tekstStreng = "jeg soker noe"
        val jsonInternalSoknad =
            createEmptyJsonInternalSoknad("12345678901", true)
                .apply { soknad.data.begrunnelse.hvaSokesOm = tekstStreng }

        jsonInternalSoknad.humanifyHvaSokesOm()

        assertThat(jsonInternalSoknad.soknad.data.begrunnelse.hvaSokesOm).isEqualTo(tekstStreng)
    }
}

private fun createDigisosSak(
    fiksDigisosId: String = "90ab535d-86a8-4eee-8292-e52920770b1a",
    kommunenummer: String = "0301",
    sistEndret: Long = 0L,
): DigisosSak =
    DigisosSak(
        fiksDigisosId,
        "123",
        "123",
        kommunenummer,
        sistEndret,
        null,
        null,
        DigisosSoker("123", emptyList(), 0L),
        null,
    )

private fun createJsonDigisosSoker(hendelser: List<JsonHendelse> = emptyList()): JsonDigisosSoker = JsonDigisosSoker().withHendelser(hendelser)

private fun createMottattHendelse(tidspunkt: String): JsonHendelse =
    JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withHendelsestidspunkt(tidspunkt)
        .withStatus(JsonSoknadsStatus.Status.MOTTATT)

private fun createPastUtbetaling(
    tidspunkt: String,
    utbetalingstidspunkt: String,
): JsonHendelse =
    JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withHendelsestidspunkt(tidspunkt)
        .withUtbetalingsdato(utbetalingstidspunkt)
        .withStatus(JsonUtbetaling.Status.UTBETALT)

private fun createUpcomingUtbetaling(
    tidspunkt: String,
    forfallsdato: String,
): JsonHendelse =
    JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withHendelsestidspunkt(tidspunkt)
        .withForfallsdato(forfallsdato)
        .withStatus(JsonUtbetaling.Status.PLANLAGT_UTBETALING)
