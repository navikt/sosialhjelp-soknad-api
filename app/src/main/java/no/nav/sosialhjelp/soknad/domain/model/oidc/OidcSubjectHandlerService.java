package no.nav.sosialhjelp.soknad.domain.model.oidc;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcSubjectHandlerService implements SubjectHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(OidcSubjectHandlerService.class);

    public String getUserIdFromToken() {
        return getTokenValidationContext().getClaims("selvbetjening").getSubject();
    }

    public String getToken() {
        return getTokenValidationContext().getJwtToken("selvbetjening").getTokenAsString();
    }

    public String getConsumerId() {
        String consumerId = System.getProperty("systemuser.username");
        return consumerId != null ? consumerId : "srvsoknadsosialhje";
    }

    private TokenValidationContext getTokenValidationContext() {
        TokenValidationContext tokenValidationContext = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext();
        if (tokenValidationContext == null) {
            logger.error("Could not find TokenValidationContext. Possibly no token in request and request was not captured by token-validation filters.");
            throw new RuntimeException("Could not find TokenValidationContext. Possibly no token in request.");
        }
        return tokenValidationContext;
    }
}
