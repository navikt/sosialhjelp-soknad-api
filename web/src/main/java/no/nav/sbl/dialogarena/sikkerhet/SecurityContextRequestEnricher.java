package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.security.tilgangskontroll.policy.enrichers.PolicyRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;
import no.nav.modig.security.tilgangskontroll.utils.AttributeUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;

public class SecurityContextRequestEnricher implements PolicyRequestEnricher {

    public PolicyRequest enrich(PolicyRequest request) {
        if (OidcFeatureToggleUtils.getUserId() == null) {
            return request;
        } else {
            String uid = OidcFeatureToggleUtils.getUserId();
            String consumerId = OidcFeatureToggleUtils.getConsumerId();
            return request.copyAndAppend(AttributeUtils.subjectId(SubjectAttribute.ACCESS_SUBJECT, uid), AttributeUtils.subjectId(SubjectAttribute.CONSUMER, consumerId));
        }
    }
}
