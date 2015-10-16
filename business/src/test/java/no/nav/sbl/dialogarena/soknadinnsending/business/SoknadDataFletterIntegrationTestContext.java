package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.StartDatoService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@Import(value = {DatabaseTestContext.class})
@EnableTransactionManagement()
@Configuration
public class SoknadDataFletterIntegrationTestContext {
    @Inject
    private DataSource dataSource;

    @Bean
    public SoknadDataFletter fletter() {
        return new SoknadDataFletter();
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return new HenvendelseService();
    }

    @Bean
    public SendSoknadPortType sendSoknadEndpoint() {
        SendSoknadPortType portType = mock(SendSoknadPortType.class);
        return portType;
    }

    @Bean
    public SendSoknadPortType sendSoknadSelftestEndpoint() {
        SendSoknadPortType portType = mock(SendSoknadPortType.class);
        return portType;
    }

    @Bean
    public FillagerService fillagerService() {
        return new FillagerService();
    }

    @Bean
    public FilLagerPortType fillagerEndpoint() {
        FilLagerPortType mock = mock(FilLagerPortType.class);
        return mock;
    }

    @Bean
    public FilLagerPortType fillagerSelftestEndpoint() {
        return mock(FilLagerPortType.class);
    }

    @Bean
    public VedleggService vedleggService() {
        return new VedleggService();
    }

    @Bean
    public SoknadRepository soknadInnsendingRepository() {
        return new SoknadRepositoryJdbc();
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
    public StartDatoService startDatoService(){
        return new StartDatoService();
    }
}