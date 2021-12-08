package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadMetricsService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SoknadService.class,
        InnsendtSoknadService.class,
        EttersendingService.class,
        DigisosApiService.class,
        SoknadMetricsService.class,
        HenvendelseService.class,
        TextService.class,
        OpplastetVedleggService.class,
})
public class ServiceConfig {
}
