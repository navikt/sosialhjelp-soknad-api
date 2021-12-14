package no.nav.sosialhjelp.soknad.api.featuretoggle

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.sosialhjelp.metrics.aspects.Timed
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.web.utils.Constants
import org.apache.commons.lang3.StringUtils
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Controller
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Configuration
@Import(
    FeatureToggleRessurs::class
)
open class FeatureToggleConfig

@Controller
@ProtectedWithClaims(issuer = Constants.SELVBETJENING, claimMap = [Constants.CLAIM_ACR_LEVEL_4])
@Path("/feature-toggle")
@Produces(MediaType.APPLICATION_JSON)
@Timed
open class FeatureToggleRessurs {

    @GET
    open fun featureToggles(): Map<String, Boolean> {
        val uid = SubjectHandler.getUserId()
        val featureToggles: MutableMap<String, Boolean> = HashMap()
        featureToggles["modalV2"] = FeatureToggleUtils.enableModalV2(uid)
        return featureToggles
    }
}

object FeatureToggleUtils {
    fun enableModalV2(uid: String?): Boolean {
        return if (uid == null || !StringUtils.isNumeric(uid)) {
            false
        } else {
            uid.toLong() % 2 == 0L
        }
    }
}
