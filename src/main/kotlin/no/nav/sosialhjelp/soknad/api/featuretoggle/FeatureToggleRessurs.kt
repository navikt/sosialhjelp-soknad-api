package no.nav.sosialhjelp.soknad.api.featuretoggle

import io.getunleash.Unleash
import no.nav.sosialhjelp.soknad.app.Constants
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/feature-toggle", produces = [MediaType.APPLICATION_JSON_VALUE])
class FeatureToggleRessurs(
    private val unleash: Unleash,
) {
    @GetMapping
    fun featureToggles(): Map<String, Boolean> = unleash.more().evaluateAllToggles().associate { it.name to it.isEnabled }
}
