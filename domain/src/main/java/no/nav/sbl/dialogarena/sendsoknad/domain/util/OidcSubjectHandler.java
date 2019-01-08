package no.nav.sbl.dialogarena.sendsoknad.domain.util;

import no.nav.security.oidc.context.OIDCValidationContext;
import no.nav.security.oidc.jaxrs.OidcRequestContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OidcSubjectHandler {
    private static final Logger logger = LoggerFactory.getLogger(OidcSubjectHandler.class);
    public final static String OIDC_SUBJECT_HANDLER_KEY = "no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler";

    public static OidcSubjectHandler getSubjectHandler() {
        String subjectHandlerImplementationClass = OidcSubjectHandler.class.getName();

        if (isStaticAllowed() && StringUtils.isNotBlank(System.getProperty(OIDC_SUBJECT_HANDLER_KEY))) {
            subjectHandlerImplementationClass = resolveProperty(OIDC_SUBJECT_HANDLER_KEY);
        }

        try {
            Class<?> clazz = Class.forName(subjectHandlerImplementationClass);
            return (OidcSubjectHandler) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException var2) {
            throw new RuntimeException("Could not configure platform dependent subject handler", var2);
        }
    }

    public String getUserIdFromToken() {
        return getOidcValidationContext().getClaims("selvbetjening").getClaimSet().getSubject();
    }

    public String getToken() {
        return getOidcValidationContext().getToken("selvbetjening").getIdToken();
    }

    private OIDCValidationContext getOidcValidationContext() {
        OIDCValidationContext oidcValidationContext = OidcRequestContext.getHolder().getOIDCValidationContext();
        if (oidcValidationContext == null) {
            logger.error("Could not find OIDCValidationContext. Possibly no token in request and request was not captured by OIDC-validation filters.");
            throw new RuntimeException("Could not find OIDCValidationContext. Possibly no token in request.");
        }
        return oidcValidationContext;
    }

    private static boolean isStaticAllowed() {
        // Burde ha en sjekk på at det er test so kjøres...
//        return "true".equalsIgnoreCase(System.getProperty("tillatmock"));
        return true;
    }

    private static String resolveProperty(String key) {
        String value = System.getProperty(key);
        if (value != null) {
            logger.debug("Setting " + key + "={} from System.properties", value);
        }
        return value;
    }
}
