package no.nav.sosialhjelp.soknad.api.featuretoggle

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.soknad.app.Constants
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@RequestMapping("/feature-toggle", produces = [MediaType.APPLICATION_JSON_VALUE])
open class FeatureToggleRessurs {

    @GetMapping
    open fun featureToggles(): Map<String, Boolean> {
        val featureToggles: MutableMap<String, Boolean> = HashMap()
        // add toggles for frontend
        return featureToggles
    }
}
