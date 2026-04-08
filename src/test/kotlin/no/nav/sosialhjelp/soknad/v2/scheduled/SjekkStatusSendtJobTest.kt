package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusSendtJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SjekkStatusSendtJobTest : AbstractJobTest() {
    @Autowired
    private lateinit var sjekkStatusSendtJob: SjekkStatusSendtJob

    @MockkSpyBean
    private lateinit var metricsService: SoknadMottattMetricsService

    private var capturedOutput: CapturingSlot<Int> = slot()

    @BeforeEach
    fun setup() {
        every { metricsService.setAntallGamleSoknaderStatusSendt(capture(capturedOutput)) } just Runs
        metadataRepository.deleteAll()
        mockkObject(TimestampUtil)
    }

    @AfterEach
    fun teardown() {
        unmockkObject(TimestampUtil)
    }

    @Test
    fun `Soknad sendt mandag kl 12 er for gammel onsdag etter kl 12`() {
        every { metricsService.doInitialize() } just Runs

        val monday = findPreviousDayOfWeek(DayOfWeek.MONDAY, 12)
        val tuesday = findPreviousDayOfWeek(DayOfWeek.TUESDAY, 12)

        createSoknadSendtAtDay(monday)
        createSoknadSendtAtDay(tuesday)

        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(1)
    }

    @Test
    fun `Soknad sendt i helg kl 12 er ikke for gammel pa tirsdag etter kl 12`() {
        val saturday = findPreviousDayOfWeek(DayOfWeek.SATURDAY, 12)
        val sunday = findPreviousDayOfWeek(DayOfWeek.SUNDAY, 12)

        createSoknadSendtAtDay(saturday)
        createSoknadSendtAtDay(sunday)

        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(sunday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(0)
    }

    @Test
    fun `Soknad sendt i helg kl 12 er for gammel pa onsdag etter kl 12`() {
        val saturday = findPreviousDayOfWeek(DayOfWeek.SATURDAY, 12)
        val sunday = findPreviousDayOfWeek(DayOfWeek.SUNDAY, 12)

        createSoknadSendtAtDay(saturday)
        createSoknadSendtAtDay(sunday)

        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(sunday.plusDays(3), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(2)
    }

    private fun createSoknadSendtAtDay(localDateTime: LocalDateTime) {
        opprettSoknadMetadata(
            status = SoknadStatus.SENDT,
            opprettetDato = localDateTime.minusHours(1),
            innsendtDato = localDateTime,
        )
            .also { metadataRepository.save(it) }
    }

    private fun setTimestampForJob(
        timestamp: LocalDateTime,
        hour: Int,
    ): LocalDateTime {
        return LocalDateTime.of(timestamp.toLocalDate(), LocalTime.of(hour, 0))
    }

    private fun findPreviousDayOfWeek(
        dayOfWeek: DayOfWeek,
        hour: Int,
    ): LocalDateTime {
        var timestampAtHour = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, 0))
        if (timestampAtHour.dayOfWeek == dayOfWeek) return timestampAtHour

        while (timestampAtHour.dayOfWeek != dayOfWeek) {
            timestampAtHour = timestampAtHour.minusDays(1)
        }
        return timestampAtHour
    }
}
