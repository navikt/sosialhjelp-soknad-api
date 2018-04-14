package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        VedleggService.class,
        VedleggOriginalFilerService.class,
        LandService.class,
        SoknadService.class,
        InnsendtSoknadService.class,
        StartDatoUtil.class,
        FaktaService.class,
        SoknadDataFletter.class,
        MigrasjonHandterer.class,
        AlternativRepresentasjonService.class,
        EkstraMetadataService.class,
        EttersendingService.class,
        EttersendelseVedleggService.class,
        XmlService.class,
        SynligeFaktaService.class,
        SoknadMetricsService.class,
        FillagerService.class,
        HenvendelseService.class
})
public class ServiceConfig {
}
