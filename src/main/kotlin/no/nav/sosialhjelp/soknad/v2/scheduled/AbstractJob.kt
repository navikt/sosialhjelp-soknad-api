package no.nav.sosialhjelp.soknad.v2.scheduled

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger

abstract class AbstractJob(
    private val leaderElection: LeaderElection,
    private val jobName: String,
    private val logger: Logger,
) {
    protected fun doInJob(function: () -> Unit) {
        runBlocking {
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
}
