package no.nav.sbl.dialogarena.saml;

import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.OpenAmTokenCredential;
import no.nav.modig.core.context.TestSubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.SluttBruker;

import javax.security.auth.Subject;

import static org.junit.Assert.*;

public class StaticSubjectHandler extends TestSubjectHandler {
    private static final Subject DEFAULT_SUBJECT = new Subject();
    private static Subject subject;

    public StaticSubjectHandler() {
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject newSubject) {
        subject = newSubject;
    }

    public void reset() {
        this.setSubject(DEFAULT_SUBJECT);
    }

    static {
        DEFAULT_SUBJECT.getPrincipals().add(SluttBruker.eksternBruker("01015245464"));
        DEFAULT_SUBJECT.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        DEFAULT_SUBJECT.getPublicCredentials().add(new OpenAmTokenCredential("01015245464-4"));
        DEFAULT_SUBJECT.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        subject = DEFAULT_SUBJECT;
    }
}