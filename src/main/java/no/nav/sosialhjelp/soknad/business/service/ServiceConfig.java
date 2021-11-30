package no.nav.sosialhjelp.soknad.business.service;

import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.SoknadUnderArbeidService;
import no.nav.sosialhjelp.soknad.business.service.dialog.SistInnsendteSoknadService;
import no.nav.sosialhjelp.soknad.business.service.digisosapi.DigisosApiService;
import no.nav.sosialhjelp.soknad.business.service.dittnav.DittNavMetadataService;
import no.nav.sosialhjelp.soknad.business.service.informasjon.PabegynteSoknaderService;
import no.nav.sosialhjelp.soknad.business.service.minesaker.MineSakerMetadataService;
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
        InnsendingService.class,
        SoknadUnderArbeidService.class,
        TextService.class,
        OpplastetVedleggService.class,
        MineSakerMetadataService.class,
        DittNavMetadataService.class,
        PabegynteSoknaderService.class,
        SistInnsendteSoknadService.class
})
public class ServiceConfig {
}
