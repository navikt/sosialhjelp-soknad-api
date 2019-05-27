package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.OppgaveHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.fillager.FillagerRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.midlertidig.VedleggConverter;
import no.nav.sbl.sosialhjelp.midlertidig.WebSoknadConverter;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.sendtsoknad.VedleggstatusRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.time.Clock;

import static org.mockito.Mockito.mock;

@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
@Configuration
public class SoknadDataFletterIntegrationTestContext {
    @Inject
    private DataSource dataSource;

    @Bean
    public Clock clock(){ return Clock.systemDefaultZone(); }

    @Bean
    public SoknadDataFletter fletter() {
        return new SoknadDataFletter();
    }

    @Bean
    public EkstraMetadataService ekstraMetadataService() {
        return new EkstraMetadataService();
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return new HenvendelseService();
    }

    @Bean
    public SoknadMetadataRepository soknadMetadataRepository() {
        return mock(SoknadMetadataRepository.class);
    }

    @Bean
    public FillagerService fillagerService() {
        return new FillagerService();
    }

    @Bean
    public FillagerRepository fillagerRepository() {
        return mock(FillagerRepository.class);
    }

    @Bean
    public VedleggService vedleggService() {
        return new VedleggService();
    }

    @Bean
    public OpplastetVedleggService opplastetVedleggService() {
        return new OpplastetVedleggService();
    }

    @Bean
    public SoknadRepository soknadInnsendingRepository() {
        return new SoknadRepositoryJdbc();
    }

    @Bean
    public HendelseRepository hendelseRepository() {
        return new HendelseRepositoryJdbc();
    }

    @Bean
    public VedleggRepository vedleggRepository() {
        return new VedleggRepositoryJdbc();
    }

    @Bean
    public RepositoryTestSupport testSupport() {
        return new TestSupport(dataSource);
    }

    @Bean
    public Kodeverk kodeverk() {
        return mock(Kodeverk.class);
    }

    @Bean
    public SoknadService soknadService() {
        return new SoknadService();
    }

    @Bean
    public WebSoknadConfig webSoknadConfig() {
        return new WebSoknadConfig();
    }

    @Bean
    public KravdialogInformasjonHolder kravdialogInformasjonHolder(){
        return new KravdialogInformasjonHolder();
    }

    @Bean
    public FaktaService faktaService(){
        return new FaktaService();
    }

    @Bean
    public NavMessageSource navMessageSource(){
        return new NavMessageSource();
    }

    @Bean
    public StartDatoUtil startDatoService(){
        return new StartDatoUtil();
    }

    @Bean
    public EttersendingService ettersendingService() { return new EttersendingService(); }

    @Bean
    public XmlService xmlService() { return new XmlService(); }

    @Bean
    public SoknadMetricsService metricsService() {
        return mock(SoknadMetricsService.class);
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