package no.nav.sosialhjelp.soknad.app.soknadlock

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

/**
 * Denne klassen henter data fra SoknadLockManager og rapporterer til Prometheus.
 * Se også SoknadLockPushMetrics, som kalles av SoknadLockManager for å rapportere data.
 *
 * @property meterRegistry Prometheus MeterRegistry
 * @property lockManager SoknadLockManager for å hente data om tilstanden på lockMap.
 */
@Component
@Suppress("unused")
class SoknadLockPullMetrics(
    private val meterRegistry: MeterRegistry,
    private val lockManager: SoknadLockManager,
) {
    private val metricsGauges = mutableListOf<Gauge>()

    init {
        metricsGauges.add(
            Gauge.builder("soknad_lock_map_size") { lockManager.getLockMapSize().toDouble() }
                .description("Number of locks in the lock map")
                .register(meterRegistry),
        )

        metricsGauges.add(
            Gauge.builder("soknad_lock_held_count") { lockManager.getNumLocks().toDouble() }
                .description("Number of locks currently held")
                .register(meterRegistry),
        )
    }
}
