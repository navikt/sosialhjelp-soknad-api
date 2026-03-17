package no.nav.sosialhjelp.soknad.v2.scheduled.patch

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class OppdaterOpprettetTimestampPatch(
    private val metadataRepository: SoknadMetadataRepository,
    private val oppdaterService: OppdaterService,
) {
    private var shouldRun = true

    @Scheduled(cron = "0 15 14 * * *")
    fun oppdaterOpprettetTimestamp() {
        if (!shouldRun) return

        logger.info("***JOB*** Starting job: Oppdaterer opprettet-timestamp for soknader opprettet for mer enn 1 time siden.")

        val soknadIds = metadataRepository.findSoknadIdsOlderThan(nowWithMillis().minusHours(1))
        logger.info("***JOB*** Fant ${soknadIds.size} soknader som skal oppdateres.")

        val start = LocalDateTime.now()

        oppdaterService.updateAllMetadatas(soknadIds)

        val end = LocalDateTime.now()

        logger.info("***JOB*** Ferdig med oppdatering av opprettet-timestamp for ${soknadIds.size} soknader. Tok: ${Duration.between(start, end)}")

        shouldRun = false
    }

    companion object {
        private val logger by logger()
    }
}
