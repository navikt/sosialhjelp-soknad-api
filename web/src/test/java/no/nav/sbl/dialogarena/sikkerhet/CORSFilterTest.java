package no.nav.sbl.dialogarena.sikkerhet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;


public class CORSFilterTest {

    private final CORSFilter corsFilter = new CORSFilter();
    private final String unknownOrigin = "https://www.unknown.no";

    @After
    public void tearDown() {
        System.clearProperty("environment.name");
    }

    @Test
    public void setCorsHeaders_inProdWithUnknownOrigin_shouldNotSetCorsHeaders() {
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();
        corsFilter.setCorsHeaders(httpResponse, unknownOrigin);
        Assert.assertEquals(0, httpResponse.getHeaderNames().size());
    }

    @Test
    public void setCorsHeaders_inProdWithTrustedOrigin_shouldSetCorsHeaders() {
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        String trustedOrigin = "https://www.nav.no";
        corsFilter.setCorsHeaders(httpResponse, trustedOrigin);

        Assert.assertTrue(httpResponse.containsHeader("Access-Control-Allow-Headers"));
        Assert.assertTrue(httpResponse.containsHeader("Access-Control-Allow-Methods"));
        Assert.assertTrue(httpResponse.containsHeader("Access-Control-Allow-Credentials"));
        Assert.assertEquals(trustedOrigin, httpResponse.getHeader("Access-Control-Allow-Origin"));
    }

    @Test
    public void setCorsHeaders_inTestWithUnknownOrigin_shouldSetCorsHeaders() {
        System.setProperty("environment.name", "q0");
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        corsFilter.setCorsHeaders(httpResponse, unknownOrigin);

        Assert.assertTrue(httpResponse.containsHeader("Access-Control-Allow-Headers"));
        Assert.assertTrue(httpResponse.containsHeader("Access-Control-Allow-Methods"));
        Assert.assertTrue(httpResponse.containsHeader("Access-Control-Allow-Credentials"));
        Assert.assertEquals(unknownOrigin, httpResponse.getHeader("Access-Control-Allow-Origin"));
    }
}