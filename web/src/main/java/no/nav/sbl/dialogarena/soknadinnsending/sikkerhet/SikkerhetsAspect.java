package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;


import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
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

    @Inject
    private SendSoknadService soknadService;

    @Pointcut("@annotation(no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad)")
    public void requestMapping() {
    }

    @Before(value = "requestMapping() && args(soknadId, ..) && @annotation(tilgang)", argNames = "soknadId, tilgang")
    public void sjekkSoknadIdModBruker(Long soknadId, SjekkTilgangTilSoknad tilgang) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (tilgang.sjekkXsrf() && request.getMethod().equals(RequestMethod.POST.name())) {
            WebSoknad soknad = soknadService.hentSoknad(soknadId);
            String brukerBehandlingId = soknad.getBrukerBehandlingId();
            if (soknad.getBehandlingskjedeId() != null) {
                brukerBehandlingId = soknad.getBehandlingskjedeId();
            }
            XsrfGenerator.sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), brukerBehandlingId);
        }
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(soknadId);
    }

    @Before(value = "requestMapping() && args(brukerbehandlingsId, ..)", argNames = "brukerbehandlingsId")
    public void sjekkMetoderMedBrukerbehandlingsId(String brukerbehandlingsId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(brukerbehandlingsId);
    }

}
