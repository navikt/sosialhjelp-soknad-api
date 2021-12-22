//package no.nav.sosialhjelp.soknad.domain.model.oidc;
//
//import no.nav.security.token.support.core.context.TokenValidationContext;
//import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Optional;
//
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.SELVBETJENING;
//import static no.nav.sosialhjelp.soknad.web.utils.Constants.TOKENX;
//
//public class OidcSubjectHandlerService implements SubjectHandlerService {
//    private static final Logger logger = LoggerFactory.getLogger(OidcSubjectHandlerService.class);
//
//    private static final String CLAIM_PID = "pid";
//
//    public String getUserIdFromToken() {
//        if (getTokenValidationContext().hasTokenFor(TOKENX)) {
//            return getTokenValidationContext().getClaims(TOKENX).getStringClaim(CLAIM_PID);
//        }
//        return getTokenValidationContext().getClaims(SELVBETJENING).getSubject();
//    }
//
//    public String getToken() {
//        return getTokenValidationContext().getJwtToken(SELVBETJENING).getTokenAsString();
//    }
//
//    public String getConsumerId() {
//        return Optional.ofNullable(System.getProperty("systemuser.username")).orElse("srvsoknadsosialhje");
//    }
//
//    private TokenValidationContext getTokenValidationContext() {
//        return Optional.ofNullable(JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext())
//                .orElseThrow(() -> {
//                    logger.error("Could not find TokenValidationContext. Possibly no token in request and request was not captured by token-validation filters.");
//                    throw new RuntimeException("Could not find TokenValidationContext. Possibly no token in request.");
//                });
//    }
//}
