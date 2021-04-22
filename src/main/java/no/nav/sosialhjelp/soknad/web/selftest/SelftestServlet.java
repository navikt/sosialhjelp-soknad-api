package no.nav.sosialhjelp.soknad.web.selftest;


import no.nav.sosialhjelp.soknad.web.types.Pingable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.Collection;

@WebServlet(urlPatterns = "/internal/selftest")
public class SelftestServlet extends SelfTestBaseServlet {
    private ApplicationContext ctx = null;

    @Override
    public void init() throws ServletException {
        ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        super.init();
    }

    @Override
    protected Collection<? extends Pingable> getPingables() {
        return ctx.getBeansOfType(Pingable.class).values();
    }
}
