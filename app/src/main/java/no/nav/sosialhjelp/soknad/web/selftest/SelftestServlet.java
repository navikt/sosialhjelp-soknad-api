package no.nav.sosialhjelp.soknad.web.selftest;


import no.nav.sosialhjelp.soknad.web.types.Pingable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import java.util.Collection;

public class SelftestServlet extends SelfTestBaseServlet {
    private static final String APPLIKASJONS_NAVN = "Søknadsapi";
    private ApplicationContext ctx = null;

    @Override
    public void init() throws ServletException {
        ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        super.init();
    }
    @Override
    protected String getApplicationName() {
        return APPLIKASJONS_NAVN;
    }

    @Override
    protected Collection<? extends Pingable> getPingables() {
        return ctx.getBeansOfType(Pingable.class).values();
    }
}
