package no.nav.sbl.dialogarena.filter;

import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

public class FakeLoginFilter implements Filter {

    private static final Logger logger = getLogger(FakeLoginFilter.class);
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    // Checkstyle tror det er redundante Exceptions
    // CHECKSTYLE:OFF
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (req.getRequestURI().matches("^(.*internal/selftest.*)|(.*index.html)|(.*feil.*)|((.*)\\.(js|css|jpg))")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (req.getParameter("fnr") != null) {
            req.getSession().setAttribute("fnr", req.getParameter("fnr"));
        }

        String header = req.getHeader("Authorization");
        String fnr  = getFnr(req);
        //SubjectHandlerUtils.setEksternBruker(fnr, 4, null);
        if (header != null && fnr != null) {
            SsoToken ssoToken = SsoToken.oidcToken(header.substring(6), Collections.emptyMap());
            Subject subject = new Subject(fnr, IdentType.EksternBruker, ssoToken);
            SubjectHandler.withSubject(subject, () -> {
                filterChain.doFilter(servletRequest, servletResponse);
            });
        } else {
            //HttpServletResponse res = (HttpServletResponse) servletResponse;
            //res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(servletRequest, servletResponse);
        }

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
