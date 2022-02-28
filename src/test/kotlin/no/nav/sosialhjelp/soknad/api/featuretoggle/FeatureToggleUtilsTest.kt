package no.nav.sosialhjelp.soknad.api.featuretoggle

import io.mockk.mockk
import no.finn.unleash.Unleash
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FeatureToggleUtilsTest {

    private val unleash: Unleash = mockk()
    private val featureToggleRessurs = FeatureToggleRessurs(unleash)

    @Test
    fun leggTilBarnTest() {
        val response = featureToggleRessurs.featureToggles()
        assertThat(response["leggeTilBarn"]).isFalse
    }
}
