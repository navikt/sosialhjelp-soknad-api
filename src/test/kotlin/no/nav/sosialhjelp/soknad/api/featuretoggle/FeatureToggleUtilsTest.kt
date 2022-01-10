package no.nav.sosialhjelp.soknad.api.featuretoggle

import io.mockk.every
import io.mockk.mockk
import no.finn.unleash.Unleash
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class FeatureToggleUtilsTest {

    private val unleash: Unleash = mockk()
    private val featureToggleRessurs = FeatureToggleRessurs(unleash)

    @Test
    fun leggTilBarnTest() {
        every { unleash.isEnabled(any(), any<Boolean>()) } returns true

        val response = featureToggleRessurs.featureToggles();
        Assertions.assertThat(response["leggeTilBarn"]).isTrue
    }
}
