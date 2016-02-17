package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.OpenAmTokenCredential;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.SluttBruker;
import no.nav.modig.core.exception.AuthorizationException;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.security.auth.Subject;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.junit.Assert.fail;

public class XsrfGeneratorTest {

    @Test
    public void skalGenerereBasertPaaInput() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        String token = XsrfGenerator.generateXsrfToken("1L");
        String tokenYesterday = XsrfGenerator.generateXsrfToken("1L", new DateTime().minusDays(1).toString("yyyyMMdd"));
        XsrfGenerator.sjekkXsrfToken(token, "1L");
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, "1L");
        sjekkAtMetodeKasterException(token, 2L);
        ((StaticSubjectHandler) StaticSubjectHandler.getSubjectHandler()).setSubject(newSubject());
        sjekkAtMetodeKasterException(token, 1L);
        ((StaticSubjectHandler) StaticSubjectHandler.getSubjectHandler()).reset();
    }

    private void sjekkAtMetodeKasterException(String token, long soknadId) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, "soknadId");
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
        }
    }

    private Subject newSubject() {
        Subject subject = new Subject();
        subject.getPrincipals().add(SluttBruker.eksternBruker("98989898989"));
        subject.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        subject.getPublicCredentials().add(new OpenAmTokenCredential("98989898989-4"));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        return subject;
    }


}
