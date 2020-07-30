package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
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
    private SubjectHandlerWrapper subjectHandlerWrapper;

    @Test
    public void skalGenerereBasertPaaInput() {
        String behandlingsId = "soknadId";

        when(subjectHandlerWrapper.getOIDCTokenAsString()).thenReturn("Token2");

        String token = XsrfGenerator.generateXsrfToken(behandlingsId, subjectHandlerWrapper.getOIDCTokenAsString());
        String tokenYesterday = XsrfGenerator.generateXsrfToken(behandlingsId, new DateTime().minusDays(1).toString("yyyyMMdd"), subjectHandlerWrapper.getOIDCTokenAsString());
        XsrfGenerator.sjekkXsrfToken(token, behandlingsId, subjectHandlerWrapper.getOIDCTokenAsString());
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, behandlingsId, subjectHandlerWrapper.getOIDCTokenAsString());
        sjekkAtMetodeKasterException(token, "2L", subjectHandlerWrapper.getOIDCTokenAsString());

        sjekkAtMetodeKasterException(token, "1L", subjectHandlerWrapper.getOIDCTokenAsString());

    }

    private void sjekkAtMetodeKasterException(String token, String behandlingsId, String oidcToken) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, behandlingsId, oidcToken);
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
        }
    }
}
