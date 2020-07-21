package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.DefaultSubjectHandlerWrapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandlerWrapper;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi.DigisosApiService;
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
        SoknadMetricsService.class,
        HenvendelseService.class,
        InnsendingService.class,
        SoknadUnderArbeidService.class,
        TextService.class,
        OpplastetVedleggService.class,
        DefaultSubjectHandlerWrapper.class
})
public class ServiceConfig {
}
