package no.nav.sbl.dialogarena.server;

import javax.security.auth.Subject;

import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import no.nav.modig.core.context.SubjectHandler;

public class ThreadLocalSubjectHandler extends SubjectHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ThreadLocalSubjectHandler.class);

    @Override
    public Subject getSubject() {
        final ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        log.info("DEBUG ThreadLocalSubjectHandler.getSubject: 1");
        if (servletRequestAttributes == null) {
            log.info("DEBUG ThreadLocalSubjectHandler.getSubject: 1 er null");
            return null;
        }
        final Request request = (Request) servletRequestAttributes.getRequest();
        log.info("DEBUG ThreadLocalSubjectHandler.getSubject: 3 request " + request);

        final Authentication authentication = request.getAuthentication();
        log.info("DEBUG ThreadLocalSubjectHandler.getSubject: 4 authentication " + authentication);

        if (authentication instanceof Authentication.User) {
            log.info("DEBUG ThreadLocalSubjectHandler.getSubject: 5 authentication er Authentication.User. Useridentity: " + ((Authentication.User) authentication).getUserIdentity());
            return ((Authentication.User) authentication).getUserIdentity().getSubject();
        } else {
            return null;
        }
    }

}
