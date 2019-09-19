package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.DigisosApiService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.IdPortenService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.KrypteringService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        SoknadService.class,
        InnsendtSoknadService.class,
        EttersendingService.class,
        DigisosApiService.class,
        IdPortenService.class,
        KrypteringService.class,
        SoknadMetricsService.class,
        HenvendelseService.class,
        InnsendingService.class,
        SoknadUnderArbeidService.class,
        TextService.class,
        OpplastetVedleggService.class
})
public class ServiceConfig {
}
