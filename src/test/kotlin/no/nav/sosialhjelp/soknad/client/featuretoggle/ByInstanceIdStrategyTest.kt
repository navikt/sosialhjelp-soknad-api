package no.nav.sosialhjelp.soknad.client.featuretoggle

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ByInstanceIdStrategyTest {

    @Test
    fun shouldReturnFalse_instanceIdNotInMap() {
        val byInstanceIdStrategy = ByInstanceIdStrategy("local")
        val parameters = mutableMapOf("instance.id" to "dev-sbs,dev-sbs-intern")
        assertThat(byInstanceIdStrategy.isEnabled(parameters)).isFalse
    }

    @Test
    fun shoudReturnTrue_instanceIdInMap() {
        val byInstanceIdStrategy = ByInstanceIdStrategy("dev-sbs")
        val parameters = mutableMapOf("instance.id" to "dev-sbs,dev-sbs-intern")
        assertThat(byInstanceIdStrategy.isEnabled(parameters)).isTrue
    }
}
