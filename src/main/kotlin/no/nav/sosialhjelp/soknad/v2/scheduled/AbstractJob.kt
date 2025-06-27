package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

abstract class AbstractJob(
    private val leaderElection: LeaderElection,
    private val jobName: String,
    private val logger: Logger = LoggerFactory.getLogger("no.nav.sosialhjelp.soknad.v2.scheduled.AbstractJob"),
) {
    protected suspend fun doInJob(function: () -> Unit) {
        if (leaderElection.isLeader()) {
            logger.info("Starter jobb: $jobName")

            runCatching {
                withTimeoutOrNull(60.seconds) { function.invoke() }
                    ?: logger.error("$jobName tok for lang tid å starte")
            }
                .onFailure { e -> logger.error("Feil i job ($jobName)", e) }
                .getOrThrow()

            logger.info("Jobb ferdig: $jobName")
        }
    }
}
