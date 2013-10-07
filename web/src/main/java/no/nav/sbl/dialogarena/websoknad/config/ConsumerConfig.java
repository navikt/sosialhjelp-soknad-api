package no.nav.sbl.dialogarena.websoknad.config;


import no.nav.modig.cxf.TimeoutFeature;
import no.nav.modig.security.sts.utility.STSConfigurationUtility;
import no.nav.sbl.dialogarena.common.timing.TimingFeature;
import no.nav.sbl.dialogarena.websoknad.service.SendSoknadService;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v1.KodeverkPortType;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.cxf.common.util.SOAPConstants.MTOM_ENABLED;
import static org.apache.cxf.ws.security.SecurityConstants.MUST_UNDERSTAND;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Configuration
@Import(value = {
        ConsumerConfig.ServicesConfig.class,
        ConsumerConfig.SendSoknadWSConfig.class,
        ConsumerConfig.KodeverkWSConfig.class,
        ConsumerConfig.BrukerProfilWSConfig.class,
        ConsumerConfig.SelftestStsConfig.class,
        ConsumerConfig.ExternalStsConfig.class
})
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
public class ConsumerConfig {

    private static final int RECEIVE_TIMEOUT = 30000;
    private static final int CONNECTION_TIMEOUT = 10000;

    @Configuration
    public static class ServicesConfig {
        @Bean
        public SendSoknadService webSoknadService() {
            return new WebSoknadService();
        }
    }

    @Configuration
    public static class SendSoknadWSConfig {
        @Value("${soknad.webservice.henvendelse.sendsoknadservice.url}")
        private URL soknadServiceEndpoint;

        @Bean
        @Scope(SCOPE_PROTOTYPE)
        public JaxWsProxyFactoryBean sendsoknadPortTypeFactory() {
            JaxWsProxyFactoryBean jaxwsClient = getJaxWsProxyFactoryBean(soknadServiceEndpoint, SendSoknadPortType.class, "classpath:SendSoknad.wsdl");
            jaxwsClient.getFeatures().add(new TimingFeature(SendSoknadPortType.class.getSimpleName()));

            return jaxwsClient;
        }

        @Bean
        public SendSoknadPortType sendSoknadService() {
            return sendsoknadPortTypeFactory().create(SendSoknadPortType.class);
        }

        @Bean
        public SendSoknadPortType sendSoknadSelftest() {
            return sendsoknadPortTypeFactory().create(SendSoknadPortType.class);
        }
    }

    @Configuration
    public static class BrukerProfilWSConfig {

        @Value("${soknad.webservice.brukerprofil.brukerprofilservice.url}")
        private URL brukerProfilEndpoint;

        @Bean
        @Scope(SCOPE_PROTOTYPE)
        public JaxWsProxyFactoryBean brukerProfilPortTypeFactory() {
            JaxWsProxyFactoryBean jaxwsClient = getJaxWsProxyFactoryBean(brukerProfilEndpoint, BrukerprofilPortType.class, "classpath:brukerprofil/no/nav/tjeneste/virksomhet/brukerprofil/v1/Brukerprofil.wsdl");
            jaxwsClient.getFeatures().add(new TimingFeature(BrukerprofilPortType.class.getSimpleName()));

            return jaxwsClient;
        }

        @Bean
        public BrukerprofilPortType brukerProfilService() {
            return konfigurerMedHttps(brukerProfilPortTypeFactory().create(BrukerprofilPortType.class));
        }

        @Bean
        public BrukerprofilPortType brukerProfilSelftest() {
            return brukerProfilPortTypeFactory().create(BrukerprofilPortType.class);
        }
    }

    @Configuration
    public static class KodeverkWSConfig {
        @Value("${sendsoknad.webservice.kodeverk.url}")
        private URL kodeverkEndPoint;

        @Bean
        @Scope(SCOPE_PROTOTYPE)
        public JaxWsProxyFactoryBean kodeverkPortTypeFactory() {
            JaxWsProxyFactoryBean jaxwsClient = getJaxWsProxyFactoryBean(kodeverkEndPoint, KodeverkPortType.class, "classpath:kodeverk/no/nav/tjeneste/virksomhet/kodeverk/v1/Kodeverk.wsdl");

            jaxwsClient.getFeatures().add(new TimingFeature(KodeverkPortType.class.getSimpleName()));

            return jaxwsClient;
        }

        @Bean
        public KodeverkPortType kodeverkService() {
            return konfigurerMedHttps(kodeverkPortTypeFactory().create(KodeverkPortType.class));
        }

        @Bean
        public KodeverkPortType kodeverkServiceSelftest() {
            return kodeverkPortTypeFactory().create(KodeverkPortType.class);
        }
    }

    @Configuration
    public static class SelftestStsConfig {
        @Inject
        @Named("sendSoknadSelftest")
        private SendSoknadPortType sendSoknadSelftest;

        @Inject
        @Named("kodeverkServiceSelftest")
        private KodeverkPortType kodeverkServiceSelftest;

        @Inject
        @Named("brukerProfilSelftest")
        private BrukerprofilPortType brukerProfilSelftest;

        @PostConstruct
        public void setupSts() {
            STSConfigurationUtility.configureStsForSystemUser(ClientProxy.getClient(sendSoknadSelftest));
            STSConfigurationUtility.configureStsForSystemUser(ClientProxy.getClient(kodeverkServiceSelftest));
            STSConfigurationUtility.configureStsForSystemUser(ClientProxy.getClient(brukerProfilSelftest));
        }
    }

    @Configuration
    public static class ExternalStsConfig {
        @Inject
        @Named("sendSoknadService")
        private SendSoknadPortType sendSoknadPortType;

        @Inject
        @Named("kodeverkService")
        private KodeverkPortType kodeverkService;

        @Inject
        @Named("brukerProfilService")
        private BrukerprofilPortType brukerProfilService;

        @PostConstruct
        public void setupSts() {
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(sendSoknadPortType));
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(kodeverkService));
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(brukerProfilService));
        }
    }

    private static <T> T konfigurerMedHttps(T portType) {
        Client client = ClientProxy.getClient(portType);
        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();

        String property = System.getProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock");
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
        proxyFactoryBean.getFeatures().addAll(asList(new LoggingFeature(), new WSAddressingFeature(), new TimeoutFeature(RECEIVE_TIMEOUT, CONNECTION_TIMEOUT)));
        proxyFactoryBean.setWsdlLocation(wsdlURL);
        Map<String, Object> props = new HashMap<>();
        props.put(MTOM_ENABLED, "true");
        props.put(MUST_UNDERSTAND, false);
        // Denne må settes for å unngå at CXF instansierer EhCache med en non-default konfigurasjon. Denne sørger
        // for at vår konfigurasjon faktisk blir lastet.
        props.put(SecurityConstants.CACHE_CONFIG_FILE, "ehcache.xml");
        proxyFactoryBean.setProperties(props);
        return proxyFactoryBean;
    }

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        System.setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }
}