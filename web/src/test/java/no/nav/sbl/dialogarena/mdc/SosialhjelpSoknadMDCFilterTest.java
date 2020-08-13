package no.nav.sbl.dialogarena.mdc;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.modig.core.context.SubjectHandlerUtils;
import org.hamcrest.collection.IsMapContaining;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

public class SosialhjelpSoknadMDCFilterTest {
    static String SYSTEMUSER_USERNAME_KEY = "no.nav.modig.security.systemuser.username";

    @BeforeClass
    public static void setUp() {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, no.nav.modig.core.context.ThreadLocalSubjectHandler.class.getName());
        System.setProperty(SYSTEMUSER_USERNAME_KEY, "srvuser");
    }

    @AfterClass
    public static void cleanUp() {
        System.clearProperty(SubjectHandler.SUBJECTHANDLER_KEY);
        System.clearProperty(SYSTEMUSER_USERNAME_KEY);
    }

    @Test
    public void shouldAddMDCkeys() throws ServletException, IOException {
        SubjectHandlerUtils.setEksternBruker("test", 3, null);
        Map<String, String> contextMap = new HashMap<>();
        FilterChain chain = (servletRequest, servletResponse) -> contextMap.putAll(MDC.getCopyOfContextMap());

        SosialhjelpSoknadMDCFilter filter = new SosialhjelpSoknadMDCFilter();
        filter.initFilterBean();
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(contextMap, IsMapContaining.hasKey("callId"));
        assertThat(contextMap, IsMapContaining.hasEntry("consumerId", "srvuser"));
    }
}