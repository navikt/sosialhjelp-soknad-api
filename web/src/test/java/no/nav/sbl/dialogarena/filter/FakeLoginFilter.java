package no.nav.sbl.dialogarena.filter;

import no.nav.modig.core.context.SubjectHandlerUtils;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class FakeLoginFilter implements Filter {

    private static final Logger logger = getLogger(FakeLoginFilter.class);
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (isPathProtectedBySAML(request.getRequestURI())) {
            if (request.getParameter("fnr") != null) {
                request.getSession().setAttribute("fnr", request.getParameter("fnr"));
            }
            String fnr = getFnr(request);
            SubjectHandlerUtils.setEksternBruker(fnr, 4, null);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Hent Fødselsnummer fra attributt. Hvis fnr ikke er satt,
     * bruk defaultFnr som blir definert i web.xml
     * @param req
     * @return
     */
    private String getFnr(HttpServletRequest req) {
        String fnr;
        fnr = (String) req.getSession().getAttribute("fnr");

        if (fnr == null) {
            fnr = filterConfig.getInitParameter("defaultFnr");
            logger.debug("FNR ikke sendt med, bruker default fnr: {}", fnr);
        }

        return fnr;
    }

    public static final List<String> UNPROTECDED_BASE_PATHS = List.of(
            "/sosialhjelp/soknad-api/metadata/ping",
            "/sosialhjelp/soknad-api/metadata/oidc/",
            "/sendsoknad/metadata/oidc/" // For integration-tests (vil bli fjernet med ny saml i PR #421 )
    );

    static boolean isPathProtectedBySAML(String requestPath) {
        return UNPROTECDED_BASE_PATHS.stream().noneMatch(requestPath::startsWith);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}
