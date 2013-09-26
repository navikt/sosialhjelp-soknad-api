package no.nav.sbl.dialogarena.websoknad.config;


import no.nav.modig.security.sts.utility.STSConfigurationUtility;
import no.nav.sbl.dialogarena.common.kodeverk.config.KodeverkConfig;
import no.nav.sbl.dialogarena.websoknad.service.WebSoknadService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.apache.cxf.common.util.SOAPConstants.MTOM_ENABLED;
import static org.apache.cxf.ws.security.SecurityConstants.MUST_UNDERSTAND;

@Configuration
@Import(value = {KodeverkConfig.class,
        ConsumerConfig.ServicesConfig.class,
        no.nav.sbl.dialogarena.dokumentinnsending.config.ConsumerConfig.class,
        ConsumerConfig.SendSoknadWSConfig.class})
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
public class ConsumerConfig {

    @Configuration
    public static class ServicesConfig {
        @Bean
        public WebSoknadService webSoknadService() {
            return new WebSoknadService();
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
    public static class ExternalStsConfig {
        @Inject
        private SendSoknadPortType sendSoknadPortType;

        @PostConstruct
        public void setupSts() {
            STSConfigurationUtility.configureStsForExternalSSO(ClientProxy.getClient(sendSoknadPortType));
        }
    }

    private static <T> T konfigurerMedHttps(T portType) {
        Client client = ClientProxy.getClient(portType);
        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();

        String property = System.getProperty("no.nav.sbl.dialogarena.websoknad.sslMock");
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
        props.put(MTOM_ENABLED, "true");
        props.put(MUST_UNDERSTAND, false);
        proxyFactoryBean.setProperties(props);
        return proxyFactoryBean;
    }

}