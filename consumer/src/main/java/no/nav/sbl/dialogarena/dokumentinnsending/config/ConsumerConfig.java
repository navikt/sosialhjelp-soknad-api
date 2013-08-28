package no.nav.sbl.dialogarena.dokumentinnsending.config;

import no.nav.modig.security.sts.utility.STSConfigurationUtility;
import no.nav.sbl.dialogarena.common.kodeverk.config.KodeverkConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkIntegrasjon;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DefaultBrukerBehandlingServiceIntegration;
import no.nav.sbl.dialogarena.dokumentinnsending.service.DefaultSoknadService;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import org.apache.cxf.common.util.SOAPConstants;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(value = {KodeverkConfig.class,
        ConsumerConfig.ServicesConfig.class,
        ConsumerConfig.OppdaterHenvendelseBehandlingWSConfig.class,
        ConsumerConfig.BrukerBehandlingWSConfig.class,
        ConsumerConfig.BrukerProfilWSConfig.class,
        ConsumerConfig.BrukerProfilMockWSConfig.class})
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
public class ConsumerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerConfig.class);

    @Bean
    public KodeverkIntegrasjon kodeverkIntegrasjon() {
        return new KodeverkIntegrasjon();
    }

    @Configuration
    @Import(KodeverkConfig.class)
    public static class ServicesConfig {

        @Bean
        public DefaultSoknadService soknadService() {
            return new DefaultSoknadService();
        }

        @Bean
        public WebSoknadService webSoknadService() {
            return new WebSoknadService();
        }

        @Bean
        public DefaultBrukerBehandlingServiceIntegration brukerBehandlingServiceIntegration() {
            return new DefaultBrukerBehandlingServiceIntegration();
        }
    }

    @Configuration
    public static class SendSoknadWSConfig {

        @Value("${soknad.webservice.henvendelse.sendsoknadservice.url}")
        private URL soknadServiceEndpoint;

        @Bean
        public JaxWsProxyFactoryBean sendsoknadPortTypeFactory() {
            return getJaxWsProxyFactoryBean(soknadServiceEndpoint, SendSoknadPortType.class, "classpath:SendSoknad.wsdl");
        }

        @Bean
        public SendSoknadPortType sendsoknadPortType() {
            return konfigurerMedHttps(sendsoknadPortTypeFactory().create(SendSoknadPortType.class));
        }
    }

    @Configuration
    public static class BrukerBehandlingWSConfig {

        @Value("${dokumentinnsending.webservice.henvendelse.henvendelsesbehandlingservice.url}")
        private URL henvendelserEndpoint;

        @Bean
        public JaxWsProxyFactoryBean henvendelsesBehandlingPortTypeFactory() {
            return getJaxWsProxyFactoryBean(henvendelserEndpoint, HenvendelsesBehandlingPortType.class, "classpath:HenvendelsesBehandling.wsdl");
        }

        @Bean
        public HenvendelsesBehandlingPortType brukerBehandlingPortType() {
            return konfigurerMedHttps(henvendelsesBehandlingPortTypeFactory().create(HenvendelsesBehandlingPortType.class));
        }

    }

    @Configuration
    public static class OppdaterHenvendelseBehandlingWSConfig {

        @Value("${dokumentinnsending.webservice.henvendelse.oppdaterehenvendelsesbehandlingservice.url}")
        private URL henvendelserEndpoint;

        @Bean
        public JaxWsProxyFactoryBean oppdaterHenvendelsesBehandlingServicePortTypeFactory() {
            return getJaxWsProxyFactoryBean(henvendelserEndpoint, OppdatereHenvendelsesBehandlingPortType.class, "classpath:Oppdaterehenvendelsesbehandling.wsdl");
        }

        @Bean
        public OppdatereHenvendelsesBehandlingPortType oppdatereHenvendelsesBehandlingPortType() {
            return konfigurerMedHttps(oppdaterHenvendelsesBehandlingServicePortTypeFactory().create(OppdatereHenvendelsesBehandlingPortType.class));
        }

    }

    @Configuration
    public static class BrukerProfilWSConfig {

        @Value("${dokumentinnsending.webservice.brukerprofil.url}")
        private URL brukerprofilEndpoint;

        @Bean
        public JaxWsProxyFactoryBean brukerBehandlingServicePortTypeFactory() throws Exception {
            JaxWsProxyFactoryBean proxyFactoryBean = getJaxWsProxyFactoryBean(brukerprofilEndpoint, BrukerprofilPortType.class, "classpath:brukerprofil/no/nav/tjeneste/virksomhet/brukerprofil/v1/Brukerprofil.wsdl");

            Map<String, Object> props = new HashMap<>();
            props.put("schema-validation-enabled", "false");
            proxyFactoryBean.setProperties(props);
            LOGGER.info("TPS URL: {}", brukerprofilEndpoint.getHost() + ":" + brukerprofilEndpoint.getPort());
            return proxyFactoryBean;
        }

        @Bean
        public BrukerprofilPortType brukerBehandlingServicePortType() throws Exception {
            return brukerBehandlingServicePortTypeFactory().create(BrukerprofilPortType.class);
        }
    }

    @Configuration
    @Profile("mock")
    public static class BrukerProfilMockWSConfig {

        @Value("${brukerprofil.webservice.mock.brukerprofil.url}")
        private URL brukerprofilMockEndpoint;

        @Bean
        public JaxWsProxyFactoryBean brukerBehandlingServicePortTypeMock() {
            LOGGER.info("Mock URL: {}", brukerprofilMockEndpoint.getHost() + ":" + brukerprofilMockEndpoint.getPort());
            return getJaxWsProxyFactoryBean(brukerprofilMockEndpoint, BrukerprofilPortType.class, "classpath:brukerprofil/no/nav/tjeneste/virksomhet/brukerprofil/v1/Brukerprofil.wsdl");
        }

        @Bean
        public BrukerprofilPortType brukerBehandlingServicePortType() throws Exception {
            return brukerBehandlingServicePortTypeMock().create(BrukerprofilPortType.class);
        }
    }


    @Configuration
    public static class ExternalStsConfig {
        @Inject
        private OppdatereHenvendelsesBehandlingPortType oppdatereHenvendelsesBehandlingPortType;
        @Inject
        private HenvendelsesBehandlingPortType henvendelsesBehandlingPortType;
        @Inject
        private BrukerprofilPortType brukerprofilPortType;

        @PostConstruct
        public void setupSts() {
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(oppdatereHenvendelsesBehandlingPortType));
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(henvendelsesBehandlingPortType));
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(brukerprofilPortType));
        }
    }

    private static <T> T konfigurerMedHttps(T portType) {
        Client client = ClientProxy.getClient(portType);
        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();

        String property = System.getProperty("no.nav.sbl.dialogarena.dokumentinnsending.sslMock");
        if (property != null && property.equals("true")) {
            TLSClientParameters params = new TLSClientParameters();
            params.setDisableCNCheck(true);
            httpConduit.setTlsClientParameters(params);
        } else {
            httpConduit.setTlsClientParameters(new TLSClientParameters());
        }
        return portType;
    }

    private static JaxWsProxyFactoryBean getJaxWsProxyFactoryBean(URL servicePath, Class<?> serviceClass, String wsdlURL) {
        JaxWsProxyFactoryBean proxyFactoryBean = new JaxWsProxyFactoryBean();
        proxyFactoryBean.setAddress(servicePath.toString());
        proxyFactoryBean.setServiceClass(serviceClass);
        proxyFactoryBean.getFeatures().add(new WSAddressingFeature());
        proxyFactoryBean.setWsdlLocation(wsdlURL);
        Map<String, Object> props = new HashMap<>();
        props.put(SOAPConstants.MTOM_ENABLED, "true");
        proxyFactoryBean.setProperties(props);
        return proxyFactoryBean;
    }

}