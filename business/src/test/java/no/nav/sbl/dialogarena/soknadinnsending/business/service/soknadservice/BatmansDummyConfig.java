package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;


import no.nav.sbl.dialogarena.common.kodeverk.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.*;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.*;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.*;
import org.mockito.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;

import javax.inject.*;
import javax.sql.*;


@Configuration
public class BatmansDummyConfig {

    @Bean
    public HenvendelseService hendvendelseService() {
        return Mockito.mock(HenvendelseService.class);
    }

    @Bean
    public FillagerService fillagerService() {
        return Mockito.mock(FillagerService.class);
    }

    @Bean
    public Kodeverk kodeverk() {
        return Mockito.mock(Kodeverk.class);
    }

    @Bean
    public FaktaService faktaService() {
        return Mockito.mock(FaktaService.class);
    }

    @Bean
    @Named("soknadInnsendingRepository")
    public SoknadRepository lokalDb() {
        return Mockito.mock(SoknadRepository.class);
    }

    @Bean
    public WebSoknadConfig config() {
        return Mockito.mock(WebSoknadConfig.class);
    }

    @Bean
    public ApplicationContext applicationContext() {
        return Mockito.mock(ApplicationContext.class);
    }

    @Bean
    public KravdialogInformasjonHolder kravdialogInformasjonHolder() {
        return Mockito.mock(KravdialogInformasjonHolder.class);
    }

    @Bean
    public StartDatoService startDatoService() {
        return Mockito.mock(StartDatoService.class);
    }

    @Bean
    public NavMessageSource messageSource() {
        return Mockito.mock(NavMessageSource.class);
    }

    @Bean
    public DataSource dataSource() {
        return Mockito.mock(DataSource.class);
    }

    @Bean
    public XmlService xmlService() {
        return Mockito.mock(XmlService.class);
    }

    @Bean
    @Named("fillagerEndpoint")
    public FilLagerPortType filLagerEndpoint() {
        return Mockito.mock(FilLagerPortType.class);

    }

    @Bean
    @Named("fillagerSelftestEndpoint")
    public FilLagerPortType filLagerSelftestEndpoint() {
        return Mockito.mock(FilLagerPortType.class);
    }

    @Bean
    @Named("sendSoknadEndpoint")
    public SendSoknadPortType sendSoknadEndpoint() {
        return Mockito.mock(SendSoknadPortType.class);

    }

    @Bean
    @Named("sendSoknadSelftestEndpoint")
    public SendSoknadPortType sendSoknadSelftestEndpoint() {
        return Mockito.mock(SendSoknadPortType.class);
    }

    @Bean
    public SoknadService soknadService(){
        return Mockito.mock(SoknadService.class);
    }

    @Bean
    public EttersendingService ettersendingService(){
        return Mockito.mock(EttersendingService.class);

    }

}
