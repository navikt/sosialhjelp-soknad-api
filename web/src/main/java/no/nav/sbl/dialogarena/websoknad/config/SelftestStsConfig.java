package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.modig.security.sts.utility.STSConfigurationUtility;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.apache.cxf.frontend.ClientProxy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Klasse for konfigurasjon av sts som brukes av selftest
 */
@Configuration
@Import(value = {
        ConsumerConfig.SendSoknadWSConfig.class,
})
public class SelftestStsConfig {
    @Inject
    @Named("sendSoknadSelftest")
    private SendSoknadPortType sendSoknadPortType;

    @PostConstruct
    public void setupSts() {
        STSConfigurationUtility.configureStsForSystemUser(ClientProxy.getClient(sendSoknadPortType));
    }
}