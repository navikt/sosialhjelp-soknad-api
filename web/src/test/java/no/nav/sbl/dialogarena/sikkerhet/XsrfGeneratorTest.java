package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.StaticSubjectHandlerService;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import org.joda.time.DateTime;
import org.junit.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils.IS_RUNNING_WITH_OIDC;
import static org.junit.Assert.fail;

public class XsrfGeneratorTest {

    @Test
    public void skalGenerereBasertPaaInput() {
        System.setProperty(IS_RUNNING_WITH_OIDC, "true");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());

        String token = XsrfGenerator.generateXsrfToken("1L");
        String tokenYesterday = XsrfGenerator.generateXsrfToken("1L", new DateTime().minusDays(1).toString("yyyyMMdd"));
        XsrfGenerator.sjekkXsrfToken(token, "1L");
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, "1L");
        sjekkAtMetodeKasterException(token, 2L);

        ((StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService()).setFakeToken("Token2");
        ((StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService()).setUser("12345");
        sjekkAtMetodeKasterException(token, 1L);

        ((StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService()).reset();
    }

    private void sjekkAtMetodeKasterException(String token, long soknadId) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, "soknadId");
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
        }
    }
}
