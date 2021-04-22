package no.nav.sosialhjelp.soknad.web.servlet;

import no.nav.sosialhjelp.soknad.web.selftest.SelftestServlet;
import org.springframework.context.annotation.Bean;

import javax.servlet.http.HttpServlet;

public class ServletConfig {

    @Bean
    public HttpServlet selftestServlet() {
        return new SelftestServlet();
    }

}
