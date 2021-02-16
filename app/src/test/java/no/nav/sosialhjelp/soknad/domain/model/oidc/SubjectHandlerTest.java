package no.nav.sosialhjelp.soknad.domain.model.oidc;

import org.junit.After;
import org.junit.Test;

import static no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler.getSubjectHandlerService;
import static no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler.setSubjectHandlerService;
import static org.junit.Assert.*;

public class SubjectHandlerTest {

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test(expected = RuntimeException.class)
    public void setSubjectHandlerService_iProdMiljo_skalGiException() {
        System.setProperty("environment.name", "p");
        setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @Test(expected = RuntimeException.class)
    public void setSubjectHandlerService_iUkjentMiljo_skalGiException() {
        System.setProperty("environment.name", "ukjent");
        setSubjectHandlerService(new StaticSubjectHandlerService());
    }

    @Test
    public void setSubjectHandlerService_lokalt_skalSetteSubjectHandler() {
        System.setProperty("environment.name", "local");
        setSubjectHandlerService(new StaticSubjectHandlerService());
        assertEquals(StaticSubjectHandlerService.class, getSubjectHandlerService().getClass());
    }
}