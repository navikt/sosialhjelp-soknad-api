package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.FerdigSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InformasjonService.class,
        VedleggService.class,
        LandService.class,
        SoknadService.class,
        FerdigSoknadService.class,
        StartDatoUtil.class,
        FaktaService.class,
        SoknadDataFletter.class,
        EttersendingService.class,
        XmlService.class
})
public class ServiceConfig {
}
