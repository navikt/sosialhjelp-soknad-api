package no.nav.sosialhjelp.soknad.api.featuretoggle

import no.finn.unleash.Unleash
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.common.Constants
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/feature-toggle")
@Produces(MediaType.APPLICATION_JSON)
@Timed
open class FeatureToggleRessurs(
    private val unleash: Unleash
) {

    @GET
    open fun featureToggles(): Map<String, Boolean> {
        val featureToggles: MutableMap<String, Boolean> = HashMap()
        return featureToggles
    }
}
