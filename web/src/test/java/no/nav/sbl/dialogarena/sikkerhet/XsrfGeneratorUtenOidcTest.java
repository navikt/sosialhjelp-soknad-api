package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlStaticSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain.AuthenticationLevelCredential;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain.ConsumerId;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain.OpenAmTokenCredential;
import no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain.SluttBruker;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.security.auth.Subject;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static no.nav.sbl.dialogarena.sendsoknad.domain.saml.SamlSubjectHandler.SUBJECTHANDLER_KEY;
import static org.junit.Assert.fail;

public class XsrfGeneratorUtenOidcTest {

    @Test
    public void skalGenerereBasertPaaInput() {
        System.setProperty(IS_RUNNING_WITH_OIDC, "false");
        setProperty(SUBJECTHANDLER_KEY, SamlStaticSubjectHandler.class.getName());
        String token = XsrfGenerator.generateXsrfToken("1L");
        String tokenYesterday = XsrfGenerator.generateXsrfToken("1L", new DateTime().minusDays(1).toString("yyyyMMdd"));
        XsrfGenerator.sjekkXsrfToken(token, "1L");
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, "1L");
        sjekkAtMetodeKasterException(token, 2L);
        ((SamlStaticSubjectHandler) SamlStaticSubjectHandler.getSubjectHandler()).setSubject(newSubject());
        sjekkAtMetodeKasterException(token, 1L);
        ((SamlStaticSubjectHandler) SamlStaticSubjectHandler.getSubjectHandler()).reset();
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
