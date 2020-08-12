package no.nav.sbl.dialogarena.saml;

import javax.security.auth.Subject;

import no.nav.common.auth.SubjectHandler;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.Set;

public class SamlSubjectHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SamlSubjectHandler.class);

    public static Subject getSubject() {
        Optional<String> ident = SubjectHandler.getIdent();
        log.info("DEBUG SAML SubjectHandler.getSubject: 0 no.nav.common.auth.SubjectHandler.ident = " + ident);

        final ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("DEBUG SAML SubjectHandler.getSubject: 1");
        if (servletRequestAttributes == null) {
            log.info("DEBUG SAML SubjectHandler.getSubject: 1 er null");
            return null;
        }
        final Request request = (Request) servletRequestAttributes.getRequest();
        log.info("DEBUG SAML SubjectHandler.getSubject: 3 request " + request);
        final Authentication authentication = request.getAuthentication();
        log.info("DEBUG SAML SubjectHandler.getSubject: 4 authentication " + authentication);

        if (authentication instanceof Authentication.User) {
            log.info("DEBUG SAML SubjectHandler.getSubject: 5 authentication er Authentication.User. Useridentity: " + ((Authentication.User) authentication).getUserIdentity());
            return ((Authentication.User) authentication).getUserIdentity().getSubject();
        } else {
            return null;
        }
    }

    public static String getUid() {
        Subject subject = getSubject();
        if (subject == null) {
            return null;
        }

        log.info("DEBUG SAML SubjectHandler.getUid subject " + subject);

        SluttBruker sluttBruker = getTheOnlyOneInSet(subject.getPrincipals(SluttBruker.class));
        log.info("DEBUG SAML SubjectHandler.getUid sluttbruker " + sluttBruker);
        if (sluttBruker != null) {
            return sluttBruker.getName();
        }

        return null;
    }

    private static <T> T getTheOnlyOneInSet(Set<T> set) {
        log.info("DEBUG SAML SubjectHandler.getTheOnlyOneInSet set " + set);
        if (set.isEmpty()) {
            log.info("DEBUG SAML SubjectHandler.getTheOnlyOneInSet set isEmpty() true");
            return null;
        }

        T first = set.iterator().next();
        log.info("DEBUG SAML SubjectHandler.getTheOnlyOneInSet first " + first);
        if (set.size() == 1) {
            log.info("DEBUG SAML SubjectHandler.getTheOnlyOneInSet set.size=1 og first " + first);
            return first;
        }

        log.info("DEBUG SAML SubjectHandler.getTheOnlyOneInSet set.size>1 ");

        log.error("expected 1 (or zero) items, got "+set.size()+", listing them:");
        for(T item : set){
            log.error(item.toString());
        }
        throw new IllegalStateException("To many (" + set.size() + ") " + first.getClass().getName() + ". Should be either 1 (logged in) og 0 (not logged in)");
    }
}
