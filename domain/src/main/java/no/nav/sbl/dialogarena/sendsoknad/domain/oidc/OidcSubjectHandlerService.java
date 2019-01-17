package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.jaxrs.OidcRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcSubjectHandlerService implements SubjectHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(OidcSubjectHandlerService.class);

    public String getUserIdFromToken() {
        return getOidcValidationContext().getClaims("selvbetjening").getClaimSet().getSubject();
    }

    public String getToken() {
        return getOidcValidationContext().getToken("selvbetjening").getIdToken();
    }

    public String getConsumerId() {
        return System.getProperty("no.nav.modig.security.systemuser.username");
    }

    private OIDCValidationContext getOidcValidationContext() {
        OIDCValidationContext oidcValidationContext = OidcRequestContext.getHolder().getOIDCValidationContext();
        if (oidcValidationContext == null) {
            logger.error("Could not find OIDCValidationContext. Possibly no token in request and request was not captured by OIDC-validation filters.");
            throw new RuntimeException("Could not find OIDCValidationContext. Possibly no token in request.");
        }
        return oidcValidationContext;
    }
}
