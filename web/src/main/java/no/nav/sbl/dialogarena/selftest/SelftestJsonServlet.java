package no.nav.sbl.dialogarena.selftest;


import no.nav.sbl.dialogarena.common.web.selftest.SelfTestJsonBaseServlet;
import no.nav.sbl.dialogarena.types.Pingable;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import java.util.Collection;

public class SelftestJsonServlet extends SelfTestJsonBaseServlet {
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
