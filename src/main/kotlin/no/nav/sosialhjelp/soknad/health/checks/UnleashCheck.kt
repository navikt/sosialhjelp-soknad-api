package no.nav.sosialhjelp.soknad.health.checks

import no.finn.unleash.repository.FeatureToggleResponse
import no.finn.unleash.repository.ToggleFetcher
import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UnleashCheck(
    @Value("\${unleash_api_url}") private val unleashUrl: String,
    private val unleashToggleFetcher: ToggleFetcher
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "Unleash"
    override val address = unleashUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        val status = unleashToggleFetcher.fetchToggles().status
        if (status == FeatureToggleResponse.Status.CHANGED || status == FeatureToggleResponse.Status.NOT_CHANGED) {
            return
        } else {
            throw RuntimeException("Ping mot Unleash på $unleashUrl. Ga status $status")
        }
    }
}
