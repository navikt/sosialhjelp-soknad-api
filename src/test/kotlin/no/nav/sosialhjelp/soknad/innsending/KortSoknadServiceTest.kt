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
import no.nav.sosialhjelp.soknad.innsending.digisosapi.DigisosApiService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.convertToOffsettDateTimeUTCString
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.kontakt.KortSoknadUseCaseHandler
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KortSoknadServiceTest {
    private val digisosApiService: DigisosApiService = mockk()
    private val kortSoknadService: KortSoknadService =
        KortSoknadService(
            dokumentasjonService = mockk(relaxed = true),
            soknadMetadataService = mockk(relaxed = true),
            okonomiService = mockk(relaxed = true),
        )
    private val kortSoknadUseCaseHandler: KortSoknadUseCaseHandler =
        KortSoknadUseCaseHandler(
            kortSoknadService = kortSoknadService,
            mellomlagerService = mockk(relaxed = true),
            digisosApiService = digisosApiService,
            metadataService = mockk(relaxed = true),
            unleash = mockk(relaxed = true),
        )

    @Test
    fun `should not qualify if there is a recent soknad from fiks`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createMottattHendelse(nowWithMillis().minusDays(2))))
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertFalse(result)
    }

    @Test
    fun `should not qualify if there are no recent soknad from fiks`() {
        val digisosSak = createDigisosSak()
        val digisosSoker = createJsonDigisosSoker(listOf(createMottattHendelse(nowWithMillis())))

        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertFalse(result)
    }

    @Test
    fun `should qualify if there is a recent utbetaling`() {
        val digisosSak = createDigisosSak()
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(createPastUtbetaling(nowWithMillis().minusDays(10), nowWithMillis().minusDays(5))),
            )
        every { digisosApiService.getSoknaderForUser() } returns listOf(digisosSak)
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertTrue(result)
    }

    @Test
    fun `should not qualify if there is no recent utbetaling`() {
        val digisosSak = createDigisosSak()
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(
                    createPastUtbetaling(
                        nowWithMillis().minusYears(1),
                        nowWithMillis().minusMonths(11),
                    ),
                ),
            )
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertFalse(result)
    }

    @Test
    fun `should qualify if there is an upcoming utbetaling`() {
        val digisosSak = createDigisosSak()
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(
                    createUpcomingUtbetaling(
                        nowWithMillis().plusDays(5),
                        nowWithMillis().plusDays(10),
                    ),
                ),
            )
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertTrue(result)
    }

    @Test
    fun `should not qualify if there is an upcoming utbetaling more than 14 days in the future`() {
        val digisosSak = createDigisosSak()
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(
                    createUpcomingUtbetaling(
                        nowWithMillis(),
                        nowWithMillis().plusDays(20),
                    ),
                ),
            )
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertFalse(result)
    }

    @Test
    fun `should not qualify if there is an upcoming utbetaling before today`() {
        val digisosSak = createDigisosSak()
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(
                    createUpcomingUtbetaling(
                        nowWithMillis(),
                        nowWithMillis().minusDays(1),
                    ),
                ),
            )
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertFalse(result)
    }

    @Test
    fun `should not qualify if there are no recent soknader or utbetalinger`() {
        every { digisosApiService.getSoknaderForUser() } returns emptyList()
        every { digisosApiService.getInnsynsfilForSoknad(any(), any()) } returns mockk()
        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("12345678901")

        assertFalse(result)
    }

    @Test
    fun `should short circuit if it finds a qualifying soknad`() {
        val digisosSak = createDigisosSak(sistEndret = 3L)
        val digisosSak2 = createDigisosSak(fiksDigisosId = "625986e9-1fbc-482c-acd5-a702059a6fba", sistEndret = 2L)
        val digisosSak3 = createDigisosSak(fiksDigisosId = "9edaffec-5065-4819-9173-b0d5ae39063f", sistEndret = 1L)
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(
                    createUpcomingUtbetaling(
                        tidspunkt = nowWithMillis().minusDays(10),
                        forfallsdato = nowWithMillis().plusDays(10),
                        status = JsonUtbetaling.Status.UTBETALT,
                        utbetalingsdato = nowWithMillis(),
                    ),
                ),
            )
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
                digisosSak2,
                digisosSak3,
            )
        every { digisosApiService.getInnsynsfilForSoknad(digisosSak.fiksDigisosId, digisosSak.digisosSoker?.metadata ?: "") } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertTrue(result)
        verify(exactly = 1) { digisosApiService.getInnsynsfilForSoknad(any(), any()) }
    }

    @Test
    fun `should short circuit if it finds a qualifying utbetaling`() {
        val digisosSak = createDigisosSak(sistEndret = 3L)
        val digisosSak2 = createDigisosSak(fiksDigisosId = "625986e9-1fbc-482c-acd5-a702059a6fba", sistEndret = 2L)
        val digisosSak3 = createDigisosSak(fiksDigisosId = "9edaffec-5065-4819-9173-b0d5ae39063f", sistEndret = 1L)
        val digisosSoker =
            createJsonDigisosSoker(
                listOf(
                    createUpcomingUtbetaling(
                        tidspunkt = nowWithMillis().minusDays(10),
                        forfallsdato = nowWithMillis().plusDays(10),
                        status = JsonUtbetaling.Status.UTBETALT,
                        utbetalingsdato = nowWithMillis(),
                    ),
                ),
            )
        every { digisosApiService.getSoknaderForUser() } returns
            listOf(
                digisosSak,
                digisosSak2,
                digisosSak3,
            )
        every {
            digisosApiService.getInnsynsfilForSoknad(
                digisosSak.fiksDigisosId,
                digisosSak.digisosSoker?.metadata ?: "",
            )
        } returns digisosSoker

        val result = kortSoknadUseCaseHandler.isQualifiedFromFiks("0301")

        assertTrue(result)
        verify(exactly = 1) { digisosApiService.getInnsynsfilForSoknad(any(), any()) }
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

private fun createJsonDigisosSoker(
    hendelser: List<JsonHendelse> = emptyList(),
): JsonDigisosSoker = JsonDigisosSoker().withHendelser(hendelser)

private fun createMottattHendelse(tidspunkt: LocalDateTime): JsonHendelse =
    JsonSoknadsStatus()
        .withType(JsonHendelse.Type.SOKNADS_STATUS)
        .withHendelsestidspunkt(convertToOffsettDateTimeUTCString(tidspunkt))
        .withStatus(JsonSoknadsStatus.Status.MOTTATT)

private fun createPastUtbetaling(
    tidspunkt: LocalDateTime,
    utbetalingstidspunkt: LocalDateTime,
): JsonHendelse =
    JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withHendelsestidspunkt(convertToOffsettDateTimeUTCString(tidspunkt))
        .withUtbetalingsdato(convertToOffsettDateTimeUTCString(utbetalingstidspunkt))
        .withStatus(JsonUtbetaling.Status.UTBETALT)

private fun createUpcomingUtbetaling(
    tidspunkt: LocalDateTime = nowWithMillis().minusDays(10),
    forfallsdato: LocalDateTime = nowWithMillis(),
    utbetalingsdato: LocalDateTime = nowWithMillis(),
    status: JsonUtbetaling.Status = JsonUtbetaling.Status.PLANLAGT_UTBETALING,
): JsonHendelse =
    JsonUtbetaling()
        .withType(JsonHendelse.Type.UTBETALING)
        .withHendelsestidspunkt(convertToOffsettDateTimeUTCString(tidspunkt))
        .withForfallsdato(convertToOffsettDateTimeUTCString(forfallsdato))
        .withStatus(status)
        .withUtbetalingsdato(convertToOffsettDateTimeUTCString(utbetalingsdato))
