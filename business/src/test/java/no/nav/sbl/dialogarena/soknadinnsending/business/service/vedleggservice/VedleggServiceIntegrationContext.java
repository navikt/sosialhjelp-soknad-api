package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;

import static org.mockito.Mockito.mock;

import java.time.Clock;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.OpplastetVedleggService;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SystemdataUpdater;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.midlertidig.VedleggConverter;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.sendtsoknad.VedleggstatusRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;

@Configuration
@Import({VedleggServiceIntegrationMockContext.class})
public class VedleggServiceIntegrationContext {

    @Bean
    public Clock clock(){ return Clock.systemDefaultZone(); }

    @Bean
    public VedleggService vedleggService() {
        return new VedleggService();
    }

    @Bean
    public OpplastetVedleggService opplastetVedleggService() {
        return new OpplastetVedleggService();
    }

    @Bean
    public FaktaService faktaService() {
        return new FaktaService();
    }

    @Bean
    public FillagerRepository fillagerRepository() {
        return mock(FillagerRepository.class);
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
        return mock(SoknadMetadataRepository.class);
    }

    @Bean
    public HendelseRepository hendelseRepository() {
        return new HendelseRepositoryJdbc();
    }

    @Bean
    public OppgaveHandterer oppgaveHandterer() {
        return mock(OppgaveHandterer.class);
    }

    @Bean
    public SoknadUnderArbeidRepository soknadUnderArbeidRepository() {
        return mock(SoknadUnderArbeidRepository.class);
    }

    @Bean
    OpplastetVedleggRepository opplastetVedleggRepository() {
        return mock(OpplastetVedleggRepository.class);
    }

    @Bean
    WebSoknadConverter webSoknadConverter() {
        return mock(WebSoknadConverter.class);
    }

    @Bean
    VedleggConverter vedleggConverter() {
        return mock(VedleggConverter.class);
    }

    @Bean
    InnsendingService innsendingService() {
        return mock(InnsendingService.class);
    }

    @Bean
    SendtSoknadRepository sendtSoknadRepository() {
        return mock(SendtSoknadRepository.class);
    }

    @Bean
    VedleggstatusRepository vedleggstatusRepository() {
        return mock(VedleggstatusRepository.class);
    }

    @Bean
    SoknadUnderArbeidService soknadUnderArbeidService() {
        return mock(SoknadUnderArbeidService.class);
    }
    
    @Bean(autowire=Autowire.NO)
    SystemdataUpdater systemdataUpdater() {
        return mock(SystemdataUpdater.class);
    }
    
    @Bean(autowire=Autowire.NO)
    Systemdata systemdata() {
        return mock(Systemdata.class);
    }
}
