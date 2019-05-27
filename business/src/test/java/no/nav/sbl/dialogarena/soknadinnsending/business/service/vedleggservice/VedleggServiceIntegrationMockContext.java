package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;


import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.XmlService;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepositoryJdbc;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Named;
import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@Configuration
public class VedleggServiceIntegrationMockContext {

    @Bean
    public HenvendelseService hendvendelseService() {
        return mock(HenvendelseService.class);
    }

    @Bean
    public FillagerService fillagerService() {
        return mock(FillagerService.class);
    }

    @Bean
    public Kodeverk kodeverk() {
        return mock(Kodeverk.class);
    }

    @Bean
    public FaktaService faktaService() {
        return mock(FaktaService.class);
    }

    @Bean
    @Named("soknadInnsendingRepository")
    public SoknadRepository lokalDb() {
        return mock(SoknadRepository.class);
    }

    @Bean
    public WebSoknadConfig config() {
        return mock(WebSoknadConfig.class);
    }

    @Bean
    public ApplicationContext applicationContext() {
        return mock(ApplicationContext.class);
    }

    @Bean
    public KravdialogInformasjonHolder kravdialogInformasjonHolder() {
        return mock(KravdialogInformasjonHolder.class);
    }

    @Bean
    public StartDatoUtil startDatoService() {
        return mock(StartDatoUtil.class);
    }

    @Bean
    public NavMessageSource messageSource() {
        return mock(NavMessageSource.class);
    }

    @Bean
    public DataSource dataSource() {
        return mock(DataSource.class);
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager());
    }

    @Bean
    public XmlService xmlService() {
        return mock(XmlService.class);
    }

    @Bean
    public SoknadDataFletter soknadDataFletter() {
        return mock(SoknadDataFletter.class);
    }

    @Bean
    public EkstraMetadataService ekstraMetadataService() {
        return mock(EkstraMetadataService.class);
    }

    @Bean
    public SoknadService soknadService(){
        return mock(SoknadService.class);
    }

    @Bean
    public EttersendingService ettersendingService(){
        return mock(EttersendingService.class);
    }

    @Bean
    @Named("vedleggRepository")
    public VedleggRepository vedleggRepository() {
        return mock(VedleggRepositoryJdbc.class);
    }

    @Bean
    public SoknadMetricsService metricsService() {
        return mock(SoknadMetricsService.class);
    }

}
