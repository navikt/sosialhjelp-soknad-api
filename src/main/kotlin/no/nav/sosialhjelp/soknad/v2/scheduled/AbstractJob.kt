package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import kotlin.time.Duration.Companion.seconds

abstract class AbstractJob(
    private val leaderElection: LeaderElection,
    private val jobName: String,
) {
    protected suspend fun doInJob(function: () -> Unit) {
        runCatching { runWithLeaderElection(function) }
            .onFailure { logger.error("Feil i job: $jobName", it) }
            .getOrThrow()
    }

    private suspend fun runWithLeaderElection(function: () -> Unit) {
        if (leaderElection.isLeader()) runWithTimeout(function)
    }

    private suspend fun runWithTimeout(function: () -> Unit) {
        withTimeoutOrNull(60.seconds) { function.invoke() }
            ?: logger.error("$jobName tok for lang tid")
    }

    companion object {
        private val logger by logger()
    }
}
