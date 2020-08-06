package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XsrfGeneratorTest {

    @Mock
    private SubjectHandler subjectHandler;

    @Test
    public void skalGenerereBasertPaaInput() {
        String behandlingsId = "soknadId";

        when(subjectHandler.getOIDCTokenAsString()).thenReturn("Token2");

        String token = XsrfGenerator.generateXsrfToken(behandlingsId, subjectHandler.getOIDCTokenAsString());
        String tokenYesterday = XsrfGenerator.generateXsrfToken(behandlingsId, new DateTime().minusDays(1).toString("yyyyMMdd"), subjectHandler.getOIDCTokenAsString());
        XsrfGenerator.sjekkXsrfToken(token, behandlingsId, subjectHandler.getOIDCTokenAsString());
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, behandlingsId, subjectHandler.getOIDCTokenAsString());
        sjekkAtMetodeKasterException(token, "2L", subjectHandler.getOIDCTokenAsString());

        sjekkAtMetodeKasterException(token, "1L", subjectHandler.getOIDCTokenAsString());

    }

    private void sjekkAtMetodeKasterException(String token, String behandlingsId, String oidcToken) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, behandlingsId, oidcToken);
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
        }
    }
}
