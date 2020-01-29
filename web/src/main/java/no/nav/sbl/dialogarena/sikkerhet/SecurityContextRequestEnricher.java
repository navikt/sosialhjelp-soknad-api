package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.security.tilgangskontroll.policy.enrichers.PolicyRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.request.PolicyRequest;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.PolicyAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;
import no.nav.modig.security.tilgangskontroll.utils.AttributeUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;

public class SecurityContextRequestEnricher implements PolicyRequestEnricher {

    public PolicyRequest enrich(PolicyRequest request) {
        if (SubjectHandler.getUserIdFromToken() == null) {
            return request;
        } else {
            String uid = SubjectHandler.getUserIdFromToken();
            String consumerId = SubjectHandler.getConsumerId();
            return request.copyAndAppend(new PolicyAttribute[]{AttributeUtils.subjectId(SubjectAttribute.ACCESS_SUBJECT, uid), AttributeUtils.subjectId(SubjectAttribute.CONSUMER, consumerId)});
        }
    }
}
