package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import kotlin.time.Duration.Companion.seconds

abstract class AbstractJob(
    private val jobName: String,
    private val leaderElection: LeaderElection,
) {
    protected suspend fun doInJob(function: () -> Unit) {
        if (!leaderElection.isLeader()) return

        runCatching {
            withTimeoutOrNull(60.seconds) {
                function.invoke()
            }
                ?: logger.error("$jobName tok for lang tid")
        }
            .onFailure { logger.error("Feil i job: $jobName", it) }
            .getOrThrow()
    }

    companion object {
        private val logger by logger()
    }
}
