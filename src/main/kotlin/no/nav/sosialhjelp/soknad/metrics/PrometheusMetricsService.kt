package no.nav.sosialhjelp.soknad.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Service

@Service
class PrometheusMetricsService(
    private val meterRegistry: MeterRegistry,
) {
    fun createIntegerGauge(
        name: String,
        description: String? = null,
    ): IntegerGauge =
        IntegerGauge(meterRegistry, name, description)

    fun createCounter(name: String, tag: Tag? = null): Counter =
        Counter
            .builder(name)
            .apply { if (tag != null) tag(tag.key, tag.value) }
            .register(meterRegistry)

    fun increment(
        name: String,
        tag: Tag,
    ) {
        meterRegistry.counter(name, Tags.of(tag)).increment()
    }
}

class IntegerGauge(
    meterRegistry: MeterRegistry,
    name: String,
    description: String?,
) {
    private var currentValue: Int? = null

    init {
        Gauge
            .builder(name) { currentValue?.toDouble() ?: Double.NaN }
            .description(description)
            .register(meterRegistry)
    }

    fun set(value: Int) {
        currentValue = value
    }
}
