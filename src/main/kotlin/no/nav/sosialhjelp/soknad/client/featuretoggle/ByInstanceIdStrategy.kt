package no.nav.sosialhjelp.soknad.client.featuretoggle

import no.finn.unleash.strategy.Strategy

class ByInstanceIdStrategy(
    private val currentInstanceId: String
) : Strategy {

    override fun getName(): String = "byInstanceId"

    override fun isEnabled(parameters: MutableMap<String, String>?): Boolean {
        return parameters
            ?.get("instance.id")
            ?.split(",\\s*".toRegex())
            ?.any { it == currentInstanceId } ?: false
    }
}
