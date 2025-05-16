package no.nav.sosialhjelp.soknad.v2.scheduled.jobs

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.v2.json.generate.TimestampUtil.nowWithMillis
import no.nav.sosialhjelp.soknad.v2.metadata.SoknadMetadataService
import no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob
import no.nav.sosialhjelp.soknad.v2.scheduled.LeaderElection
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SlettGammelMetadataJob(
    leaderElection: LeaderElection,
    private val metadataService: SoknadMetadataService,
) : AbstractJob(jobName = "Slette gamle metadata", leaderElection) {
    @Scheduled(cron = KLOKKEN_TRE_OM_NATTEN)
    suspend fun slettGammelMetadata() =
        doInJob {
            metadataService
                .findOlderThan(nowWithMillis().minusDays(NUMBER_OF_DAYS))
                .also { soknadIds ->
                    metadataService.deleteAll(soknadIds)
                    logger.info("Slettet ${soknadIds.size} gamle metadata-innslag")
                }
        }

    companion object {
        private val logger by logger()
        private const val KLOKKEN_TRE_OM_NATTEN = "0 0 3 * * *"
        private const val NUMBER_OF_DAYS = 200L
    }
}
