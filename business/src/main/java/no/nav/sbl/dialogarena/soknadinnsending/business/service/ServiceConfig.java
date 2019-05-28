package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        LandService.class,
        SoknadService.class,
        InnsendtSoknadService.class,
        StartDatoUtil.class,
        FaktaService.class,
        SoknadDataFletter.class,
        EkstraMetadataService.class,
        EttersendingService.class,
        XmlService.class,
        SoknadMetricsService.class,
        FillagerService.class,
        HenvendelseService.class,
        WebSoknadConverter.class,
        InnsendingService.class,
        SoknadUnderArbeidService.class,
        TextService.class,
        OpplastetVedleggService.class
})
public class ServiceConfig {
}
