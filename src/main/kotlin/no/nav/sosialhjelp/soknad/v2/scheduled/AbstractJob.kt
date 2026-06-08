package no.nav.sosialhjelp.soknad.v2.scheduled

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds

abstract class AbstractJob(
    private val leaderElection: LeaderElection,
    private val jobName: String,
    private val logger: Logger,
) {
    protected fun doInJob(function: () -> Unit) {
        runBlocking {
            if (leaderElection.isLeader()) {
                val span = createSpan(jobName)
                val scope = span.makeCurrent()
                try {
                    logger.info("Starter jobb: $jobName")

                    runCatching {
                        withTimeoutOrNull(60.seconds) { function.invoke() }
                            ?: logger.error("$jobName tok for lang tid å starte")
                    }
                        .onFailure { e ->
                            logger.error("Feil i job ($jobName)", e)
                            span.recordException(e)
                            span.setStatus(StatusCode.ERROR)
                            throw e
                        }
                        .also {
                            scope.close()
                            span.end()
                        }

                    logger.info("Jobb ferdig: $jobName")
                } finally {
                    scope.close()
                    span.end()
                }
            }
        }
    }

    private fun createSpan(jobName: String): Span =
        GlobalOpenTelemetry.getTracer("sosialhjelp-soknad-api")
            .spanBuilder("scheduledJob")
            .setAttribute("job.name", jobName)
            .startSpan()
}
