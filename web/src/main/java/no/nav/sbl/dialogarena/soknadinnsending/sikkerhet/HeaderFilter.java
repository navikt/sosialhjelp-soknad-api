package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;


import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * klaaasse som legger på sikkerhets headere
 */
public class HeaderFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Frame-Options", "SAME");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        //String scp = "default-src: 'self'; script-src 'self' https://ssl.google-analytics.com http://www.google-analytics.com; media-src 'self'";
        //response.setHeader("Content-Security-Policy", scp);
        //response.setHeader("X-Content-Security-Policy", scp);
        //response.setHeader("X-WebKit-SCP", scp);
        filterChain.doFilter(request, response);
    }
}
