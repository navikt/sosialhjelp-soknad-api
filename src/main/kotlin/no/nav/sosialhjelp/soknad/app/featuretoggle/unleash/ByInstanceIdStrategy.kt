package no.nav.sosialhjelp.soknad.app.featuretoggle.unleash

import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy

class ByInstanceIdStrategy(
    private val currentInstanceId: String,
) : Strategy {
    override fun getName(): String = "byInstanceId"

    override fun isEnabled(
        parameters: MutableMap<String, String>,
        p1: UnleashContext,
    ): Boolean {
        return parameters["instance.id"]
            ?.split(",\\s*".toRegex())
            ?.any { it == currentInstanceId } ?: false
    }
}
