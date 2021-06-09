package no.nav.sosialhjelp.soknad.web.rest.ressurser.featureToggle;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.sosialhjelp.metrics.aspects.Timed;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.CLAIM_ACR_LEVEL_4;
import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
import static no.nav.sosialhjelp.soknad.web.utils.FeatureToggleUtils.enableModalV2;

@Controller
@ProtectedWithClaims(issuer = SELVBETJENING, claimMap = {CLAIM_ACR_LEVEL_4})
@Path("/feature-toggle")
@Produces(APPLICATION_JSON)
@Timed
public class FeatureToggleRessurs {

    @GET
    @Path("/")
    public Map<String, Boolean> getFeatureToggles() {
        String uid = SubjectHandler.getUserId();

        Map<String, Boolean> featureToggles = new HashMap<>();
        featureToggles.put("modalV2", enableModalV2(uid));

        return featureToggles;
    }


}
