package no.nav.sosialhjelp.soknad.v2.scheduled

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.DigisosSak
import no.nav.sosialhjelp.api.fiks.exceptions.FiksException
import no.nav.sosialhjelp.soknad.innsending.digisosapi.FiksReadClient
import no.nav.sosialhjelp.soknad.metrics.SoknadMottattMetricsService
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadata
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataServiceImpl
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadStatus
import no.nav.sosialhjelp.soknad.v2.opprettSoknadMetadata
import no.nav.sosialhjelp.soknad.v2.scheduled.jobs.SjekkStatusSendtJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.data.repository.findByIdOrNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SjekkStatusSendtJobTest : AbstractJobTest() {
    @Autowired
    private lateinit var sjekkStatusSendtJob: SjekkStatusSendtJob

    @MockkSpyBean
    private lateinit var metricsService: SoknadMottattMetricsService

    @MockkBean
    private lateinit var fiksReadClient: FiksReadClient

    @MockkSpyBean
    private lateinit var metadataService: SoknadMetadataServiceImpl

    private var capturedOutput: CapturingSlot<Int> = slot()

    @BeforeEach
    fun setup() {
        every { metricsService.setAntallGamleSoknaderStatusSendt(capture(capturedOutput)) } just Runs
        every { fiksReadClient.getDigisosSakMedSporingsId(any()) } returns
            mockk<DigisosSak> { every { digisosSoker } returns null }
        every { metadataService.updateSoknadStatus(any(), any()) } answers { callOriginal() }
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
        val tuesday = monday.plusDays(1)

        val gammelSoknad = createSoknadSendtAtDay(monday)
        val nySoknad = createSoknadSendtAtDay(tuesday)
        mockInnsynsfil(nySoknad)

        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(1)
        verify(exactly = 1) { fiksReadClient.getDigisosSakMedSporingsId(checkNotNull(gammelSoknad.digisosId)) }
        verify(exactly = 0) { fiksReadClient.getDigisosSakMedSporingsId(checkNotNull(nySoknad.digisosId)) }
        assertThat(metadataRepository.findByIdOrNull(nySoknad.soknadId)?.status).isEqualTo(SoknadStatus.SENDT)
    }

    @Test
    fun `Soknad sendt i helg kl 12 er ikke for gammel pa tirsdag etter kl 12`() {
        val saturday = findPreviousDayOfWeek(DayOfWeek.SATURDAY, 12)
        val sunday = saturday.plusDays(1)

        createSoknadSendtAtDay(saturday)
        createSoknadSendtAtDay(sunday)

        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(sunday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(0)
        verify(exactly = 0) { fiksReadClient.getDigisosSakMedSporingsId(any()) }
    }

    @Test
    fun `Soknad sendt i helg kl 12 er for gammel pa onsdag etter kl 12`() {
        val saturday = findPreviousDayOfWeek(DayOfWeek.SATURDAY, 12)
        val sunday = saturday.plusDays(1)

        createSoknadSendtAtDay(saturday)
        createSoknadSendtAtDay(sunday)

        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(sunday.plusDays(3), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(capturedOutput.captured) }
        assertThat(metadataRepository.findAll()).hasSize(2)
        assertThat(capturedOutput.captured).isEqualTo(2)
        verify(exactly = 2) { fiksReadClient.getDigisosSakMedSporingsId(any()) }
    }

    @Test
    fun `Skal bare markere gamle soknader med innsynsfil som mottatt`() {
        val monday = findPreviousDayOfWeek(DayOfWeek.MONDAY, 12)
        val medInnsynsfil = createSoknadSendtAtDay(monday)
        val utenDigisosSoker = createSoknadSendtAtDay(monday)
        mockInnsynsfil(medInnsynsfil)
        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        assertThat(metadataRepository.findByIdOrNull(medInnsynsfil.soknadId)?.status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        assertThat(metadataRepository.findByIdOrNull(utenDigisosSoker.soknadId)?.status).isEqualTo(SoknadStatus.SENDT)
        verify(exactly = 1) { metadataService.updateSoknadStatus(medInnsynsfil.soknadId, SoknadStatus.MOTTATT_FSL) }
        verify(exactly = 1) { metadataService.updateSoknadStatus(any(), any()) }
        assertThat(capturedOutput.captured).isEqualTo(1)
    }

    @Test
    fun `Skal sette metrics til 0 nar alle gamle soknader er mottatt`() {
        val monday = findPreviousDayOfWeek(DayOfWeek.MONDAY, 12)
        val metadata = createSoknadSendtAtDay(monday)
        mockInnsynsfil(metadata)
        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        assertThat(metadataRepository.findByIdOrNull(metadata.soknadId)?.status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(0) }
    }

    @Test
    fun `Skal nullstille metrics nar ingen gamle soknader gjenstar ved neste kjoring`() {
        val monday = findPreviousDayOfWeek(DayOfWeek.MONDAY, 12)
        val metadata = createSoknadSendtAtDay(monday)
        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()
        assertThat(capturedOutput.captured).isEqualTo(1)

        metadataRepository.deleteById(metadata.soknadId)
        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(1) }
        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(0) }
        verify(exactly = 1) { fiksReadClient.getDigisosSakMedSporingsId(any()) }
        assertThat(capturedOutput.captured).isZero()
    }

    @Test
    fun `Skal beholde soknad som SENDT ved Fiks-feil og behandle de andre soknadene`() {
        val monday = findPreviousDayOfWeek(DayOfWeek.MONDAY, 12)
        val feiletSoknad = createSoknadSendtAtDay(monday)
        val mottattSoknad = createSoknadSendtAtDay(monday)
        every { fiksReadClient.getDigisosSakMedSporingsId(checkNotNull(feiletSoknad.digisosId)) } throws
            FiksException("Fiks er utilgjengelig", null)
        mockInnsynsfil(mottattSoknad)
        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        assertThat(metadataRepository.findByIdOrNull(feiletSoknad.soknadId)?.status).isEqualTo(SoknadStatus.SENDT)
        assertThat(metadataRepository.findByIdOrNull(mottattSoknad.soknadId)?.status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        verify(exactly = 0) { metadataService.updateSoknadStatus(feiletSoknad.soknadId, any()) }
        assertThat(capturedOutput.captured).isEqualTo(1)
    }

    @Test
    fun `Skal telle mislykket statusoppdatering og prove igjen ved neste kjoring`() {
        val monday = findPreviousDayOfWeek(DayOfWeek.MONDAY, 12)
        val feiletSoknad = createSoknadSendtAtDay(monday)
        val mottattSoknad = createSoknadSendtAtDay(monday)
        mockInnsynsfil(feiletSoknad)
        mockInnsynsfil(mottattSoknad)
        every { metadataService.updateSoknadStatus(feiletSoknad.soknadId, SoknadStatus.MOTTATT_FSL) } throws
            DataAccessResourceFailureException("Databaseoppdatering feilet")
        every { TimestampUtil.nowWithMillis() } returns setTimestampForJob(monday.plusDays(2), 13)

        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        assertThat(metadataRepository.findByIdOrNull(feiletSoknad.soknadId)?.status).isEqualTo(SoknadStatus.SENDT)
        assertThat(metadataRepository.findByIdOrNull(mottattSoknad.soknadId)?.status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        assertThat(capturedOutput.captured).isEqualTo(1)

        every { metadataService.updateSoknadStatus(feiletSoknad.soknadId, SoknadStatus.MOTTATT_FSL) } answers { callOriginal() }
        sjekkStatusSendtJob.doCheckSoknaderStatusSendt()

        assertThat(metadataRepository.findByIdOrNull(feiletSoknad.soknadId)?.status).isEqualTo(SoknadStatus.MOTTATT_FSL)
        verify(exactly = 2) { fiksReadClient.getDigisosSakMedSporingsId(checkNotNull(feiletSoknad.digisosId)) }
        verify(exactly = 1) { fiksReadClient.getDigisosSakMedSporingsId(checkNotNull(mottattSoknad.digisosId)) }
        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(1) }
        verify(exactly = 1) { metricsService.setAntallGamleSoknaderStatusSendt(0) }
        assertThat(capturedOutput.captured).isZero()
    }

    private fun mockInnsynsfil(soknadMetadata: SoknadMetadata) {
        every { fiksReadClient.getDigisosSakMedSporingsId(checkNotNull(soknadMetadata.digisosId)) } returns
            mockk<DigisosSak> {
                every { digisosSoker } returns
                    mockk { every { metadata } returns "innsynsfil-id" }
            }
    }

    private fun createSoknadSendtAtDay(localDateTime: LocalDateTime): SoknadMetadata =
        opprettSoknadMetadata(
            status = SoknadStatus.SENDT,
            opprettetDato = localDateTime.minusHours(1),
            innsendtDato = localDateTime,
        )
            .let { metadataRepository.save(it) }

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
