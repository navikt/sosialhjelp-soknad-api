package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SikkerhetsAspect {
    @Inject
    Tilgangskontroll tilgangskontroll;

    @Pointcut("@annotation(no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad)")
    public void requestMapping() {
    }

    @Before(value = "requestMapping() && args(soknadId, ..) && @annotation(tilgang)", argNames = "soknadId, tilgang")
    public void sjekkSoknadIdModBruker(Long soknadId, SjekkTilgangTilSoknad tilgang) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (tilgang.sjekkXsrf() && request.getMethod().equals(RequestMethod.POST.name())) {
            XsrfGenerator.sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), soknadId);
        }
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(soknadId);
    }

    @Before(value = "requestMapping() && args(brukerbehandlingsId, ..)", argNames = "brukerbehandlingsId")
    public void sjekkMetoderMedBrukerbehandlingsId(String brukerbehandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(brukerbehandlingsId);
    }

}
