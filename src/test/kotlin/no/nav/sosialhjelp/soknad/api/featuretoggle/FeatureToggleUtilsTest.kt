package no.nav.sosialhjelp.soknad.api.featuretoggle

import io.mockk.mockk
import no.finn.unleash.Unleash

internal class FeatureToggleUtilsTest {

    private val unleash: Unleash = mockk()
    private val featureToggleRessurs = FeatureToggleRessurs(unleash)
}
