package no.nav.sbl.dialogarena.sendsoknad.domain.oidc;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcSubjectHandlerService implements SubjectHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(OidcSubjectHandlerService.class);

    public String getUserIdFromToken() {
        return getOidcValidationContext().getClaims("selvbetjening").getSubject();
    }

    public String getToken() {
        return getOidcValidationContext().getJwtToken("selvbetjening").getTokenAsString();
    }

    public String getConsumerId() {
        return System.getProperty("no.nav.modig.security.systemuser.username");
    }

    private TokenValidationContext getOidcValidationContext() {
        TokenValidationContext tokenValidationContext = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
        if (tokenValidationContext == null) {
            logger.error("Could not find OIDCValidationContext. Possibly no token in request and request was not captured by OIDC-validation filters.");
            throw new RuntimeException("Could not find OIDCValidationContext. Possibly no token in request.");
        }
        return tokenValidationContext;
    }
}
