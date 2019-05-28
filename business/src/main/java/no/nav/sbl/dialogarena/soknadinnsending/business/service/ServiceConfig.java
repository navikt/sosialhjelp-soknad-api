package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        LandService.class,
        SoknadService.class,
        InnsendtSoknadService.class,
        FaktaService.class,
        SoknadDataFletter.class,
        EttersendingService.class,
        SoknadMetricsService.class,
        FillagerService.class,
        HenvendelseService.class,
        InnsendingService.class,
        SoknadUnderArbeidService.class,
        TextService.class,
        OpplastetVedleggService.class
})
public class ServiceConfig {
}
