package no.nav.sbl.dialogarena.soknadinnsending.business.service.vedleggservice;


import no.nav.sbl.dialogarena.common.kodeverk.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.*;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.*;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;

import javax.inject.*;
import javax.sql.*;

import static org.mockito.Mockito.*;

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
    public StartDatoService startDatoService() {
        return mock(StartDatoService.class);
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
    public XmlService xmlService() {
        return mock(XmlService.class);
    }

    @Bean
    @Named("fillagerEndpoint")
    public FilLagerPortType filLagerEndpoint() {
        return mock(FilLagerPortType.class);
    }

    @Bean
    @Named("fillagerSelftestEndpoint")
    public FilLagerPortType filLagerSelftestEndpoint() {
        return mock(FilLagerPortType.class);
    }

    @Bean
    @Named("sendSoknadEndpoint")
    public SendSoknadPortType sendSoknadEndpoint() {
        return mock(SendSoknadPortType.class);
    }

    @Bean
    public SoknadDataFletter soknadDataFletter() {
        return mock(SoknadDataFletter.class);
    }

    @Bean
    @Named("sendSoknadSelftestEndpoint")
    public SendSoknadPortType sendSoknadSelftestEndpoint() {
        return mock(SendSoknadPortType.class);
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

}
