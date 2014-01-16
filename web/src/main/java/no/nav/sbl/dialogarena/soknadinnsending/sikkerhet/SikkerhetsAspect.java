package no.nav.sbl.dialogarena.soknadinnsending.sikkerhet;


import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Aspect
@Component
public class SikkerhetsAspect {
    @Inject
    Tilgangskontroll tilgangskontroll;

    @Pointcut("@within(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestMapping() {
    }

    @Before(value = "requestMapping() && args(soknadId, ..)", argNames = "soknadId")
    public void sjekkSoknadIdModBruker(Long soknadId) {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(soknadId);
    }
}
