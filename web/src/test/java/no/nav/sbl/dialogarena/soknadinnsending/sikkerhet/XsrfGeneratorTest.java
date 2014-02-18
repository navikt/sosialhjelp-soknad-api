package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;

import no.nav.modig.core.context.AuthenticationLevelCredential;
import no.nav.modig.core.context.OpenAmTokenCredential;
import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.domain.ConsumerId;
import no.nav.modig.core.domain.SluttBruker;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.security.auth.Subject;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class XsrfGeneratorTest {

    @Test
    public void skalGenerereBasertPaaInput() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        String token = XsrfGenerator.generateXsrfToken(1L);
        String tokenYesterday = XsrfGenerator.generateXsrfToken(1L, new DateTime().minusDays(1).toString("yyyyMMdd"));
        assertThat(XsrfGenerator.sjekkXsrfToken(token, 1L), is(true));
        assertThat(XsrfGenerator.sjekkXsrfToken(tokenYesterday, 1L), is(true));
        assertThat(XsrfGenerator.sjekkXsrfToken(token, 2L), is(false));
        ((StaticSubjectHandler) StaticSubjectHandler.getSubjectHandler()).setSubject(newSubject());
        assertThat(XsrfGenerator.sjekkXsrfToken(token, 1L), is(false));
        ((StaticSubjectHandler) StaticSubjectHandler.getSubjectHandler()).reset();
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
