package no.nav.sbl.dialogarena.saml;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.slf4j.LoggerFactory.getLogger;

public class FakeLoginFilter implements Filter {

    private static final Logger logger = getLogger(FakeLoginFilter.class);
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        Subject subject = null;
        if (OpenAmLoginFilter.isPathProtectedBySAML(request.getRequestURI())) {
            if (request.getParameter("fnr") != null) {
                request.getSession().setAttribute("fnr", request.getParameter("fnr"));
            }
            String fnr = getFnr(request);
            subject = new Subject(fnr, IdentType.EksternBruker, SsoToken.eksternOpenAM("fakeLoginFilter-Token", new HashMap<String, String>()));
        }
        SubjectHandler.withSubject(subject, () -> filterChain.doFilter(servletRequest, servletResponse));
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

    @Override
    public void destroy() {
        this.filterConfig = null;
    }

}
