package no.nav.sbl.dialogarena.mdc;

import org.hamcrest.collection.IsMapContaining;
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

    @Test
    public void shouldAddMDCkeys() throws ServletException, IOException {
        Map<String, String> contextMap = new HashMap<>();
        FilterChain chain = (servletRequest, servletResponse) -> contextMap.putAll(MDC.getCopyOfContextMap());

        SosialhjelpSoknadMDCFilter filter = new SosialhjelpSoknadMDCFilter();
        filter.initFilterBean();
        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(contextMap, IsMapContaining.hasKey("callId"));
        assertThat(contextMap, IsMapContaining.hasEntry("consumerId", "srvuser"));
    }

}