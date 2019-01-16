package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StaticOidcSubjectHandler;
import org.joda.time.DateTime;
import org.junit.Test;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.OIDC_SUBJECT_HANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.getSubjectHandler;
import static org.junit.Assert.fail;

public class XsrfGeneratorTest {

    @Test
    public void skalGenerereBasertPaaInput() {
        setProperty(OIDC_SUBJECT_HANDLER_KEY, StaticOidcSubjectHandler.class.getName());
        String token = XsrfGenerator.generateXsrfToken("1L");
        String tokenYesterday = XsrfGenerator.generateXsrfToken("1L", new DateTime().minusDays(1).toString("yyyyMMdd"));
        XsrfGenerator.sjekkXsrfToken(token, "1L");
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, "1L");
        sjekkAtMetodeKasterException(token, 2L);
        ((StaticOidcSubjectHandler) getSubjectHandler()).setFakeToken("Token2");
        ((StaticOidcSubjectHandler) getSubjectHandler()).setUser("12345");
        sjekkAtMetodeKasterException(token, 1L);
        ((StaticOidcSubjectHandler) getSubjectHandler()).reset();
    }

    private void sjekkAtMetodeKasterException(String token, long soknadId) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, "soknadId");
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
        }
    }
}
