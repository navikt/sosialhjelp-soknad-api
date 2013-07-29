package no.nav.sbl.dialogarena.soknad.config;

import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(value = {ConsumerConfig.SendSoknadWSConfig.class})
@ImportResource({"classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml"})
public class ConsumerConfig {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerConfig.class);


    @Configuration
    public static class SendSoknadWSConfig {

        @Value("${soknad.webservice.henvendelse.soknadservice.url}")
        private URL soknadServiceEndpoint;

        @Bean
        public JaxWsProxyFactoryBean sendsoknadPortTypeFactory() {
            return getJaxWsProxyFactoryBean(soknadServiceEndpoint, SendSoknadPortType.class, "classpath:SendSoknad.wsdl");
        }

        @Bean
        public SendSoknadPortType sendsoknadPortType() {
            return sendsoknadPortTypeFactory().create(SendSoknadPortType.class);
        }
    }


    private static JaxWsProxyFactoryBean getJaxWsProxyFactoryBean(URL servicePath, Class<?> serviceClass, String wsdlURL) {
        JaxWsProxyFactoryBean proxyFactoryBean = new JaxWsProxyFactoryBean();
        proxyFactoryBean.setAddress(servicePath.toString());
        proxyFactoryBean.setServiceClass(serviceClass);
        proxyFactoryBean.getFeatures().add(new WSAddressingFeature());
        proxyFactoryBean.setWsdlLocation(wsdlURL);
        Map<String, Object> props = new HashMap<>();
        proxyFactoryBean.setProperties(props);
        return proxyFactoryBean;
    }
}