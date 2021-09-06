package no.nav.sosialhjelp.soknad.web.utils;

import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException;
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService;
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.fail;

class XsrfGeneratorTest {

    @BeforeEach
    public void setUp() {
        System.setProperty("environment.name", "test");
        SubjectHandler.setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @AfterEach
    public void tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService();
        System.clearProperty("environment.name");
    }

    @Test
    void skalGenerereBasertPaaInput() {
        String token = XsrfGenerator.generateXsrfToken("1L");
        String tokenYesterday = XsrfGenerator.generateXsrfToken("1L", ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        XsrfGenerator.sjekkXsrfToken(token, "1L");
        XsrfGenerator.sjekkXsrfToken(tokenYesterday, "1L");
        sjekkAtMetodeKasterException(token, 2L);

        ((StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService()).setFakeToken("Token2");
        ((StaticSubjectHandlerService) SubjectHandler.getSubjectHandlerService()).setUser("12345");
        sjekkAtMetodeKasterException(token, 1L);
    }

    private void sjekkAtMetodeKasterException(String token, long soknadId) {
        try {
            XsrfGenerator.sjekkXsrfToken(token, "soknadId");
            fail("Kastet ikke exception");
        } catch (AuthorizationException ex) {
        }
    }
}
