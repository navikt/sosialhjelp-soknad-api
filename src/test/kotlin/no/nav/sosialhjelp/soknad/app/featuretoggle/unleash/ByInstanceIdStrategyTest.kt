package no.nav.sosialhjelp.soknad.app.featuretoggle.unleash

import io.getunleash.UnleashContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ByInstanceIdStrategyTest {
    private val unleashContext: UnleashContext = UnleashContext.builder().build()

    @Test
    fun shouldReturnFalse_instanceIdNotInMap() {
        val byInstanceIdStrategy = ByInstanceIdStrategy("local")
        val parameters = mutableMapOf("instance.id" to "dev,mock")
        assertThat(byInstanceIdStrategy.isEnabled(parameters, unleashContext)).isFalse
    }

    @Test
    fun shoudReturnTrue_instanceIdInMap() {
        val byInstanceIdStrategy = ByInstanceIdStrategy("dev")
        val parameters = mutableMapOf("instance.id" to "dev,mock")
        assertThat(byInstanceIdStrategy.isEnabled(parameters, unleashContext)).isTrue
    }
}
