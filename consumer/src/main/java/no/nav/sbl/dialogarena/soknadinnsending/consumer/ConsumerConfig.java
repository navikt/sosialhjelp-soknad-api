package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.modig.jaxws.handlers.MDCOutHandler;
import no.nav.modig.security.sts.utility.STSConfigurationUtility;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;

@Configurable
@ComponentScan
@EnableCaching
@Import(ConsumerConfig.AktorConfig.class)
public class ConsumerConfig {

    @Value("${soknad.webservice.aktor.aktorservice.url:}")
    private String aktoerEndpointUrl;

    @Configurable
    @Profile("!mockPortTypes")
    public class AktorConfig {
        @Bean
        public AktoerPortType aktorPortType() {
            return new CxfService<>(Services.AKTOER, aktoerEndpointUrl, Type.USER, AktoerPortType.class).portType();
        }

        @Bean
        public AktoerPortType aktorSelftestPortType() {
            return new CxfService<>(Services.AKTOER, aktoerEndpointUrl, Type.SYSTEM, AktoerPortType.class).portType();
        }
    }

    @SuppressWarnings("PMD.SingularField")
    private enum Services {
        AKTOER(new QName("http://nav.no/tjeneste/virksomhet/aktoer/v1/", "AktoerPortType"),
                "classpath:wsdl/no/nav/tjeneste/virksomhet/aktoer/v1/Aktoer.wsdl");

        private final QName portType;
        private final String wsdl;

        Services(QName portType, String wsdl) {
            this.portType = portType;
            this.wsdl = wsdl;
        }
    }

    private enum Type {
        SYSTEM, USER;


        public void configureSecurity(Client client) {
            if (SYSTEM.equals(this)) {
                STSConfigurationUtility.configureStsForSystemUser(client);
            } else {
                STSConfigurationUtility.configureStsForExternalSSO(client);
            }
        }
    }

    private class CxfService<T> {
        private final Services service;
        private final String endpointUrl;
        private final Type system;
        private Class<T> resultClass;

        private CxfService(Services service, String endpointUrl, Type system, Class<T> resultClass) {
            this.service = service;
            this.endpointUrl = endpointUrl;
            this.resultClass = resultClass;
            this.system = system;
        }

        private T portType() {
            JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
            factoryBean.setWsdlURL(service.wsdl);
            factoryBean.setServiceName(service.portType);
            factoryBean.setEndpointName(service.portType);
            factoryBean.setServiceClass(resultClass);
            factoryBean.setAddress(endpointUrl);
            factoryBean.getFeatures().add(new WSAddressingFeature());
            factoryBean.getFeatures().add(new LoggingFeature());
            factoryBean.getFeatures().add(new no.nav.modig.cxf.TimeoutFeature(20000));

            T port = factoryBean.create(resultClass);
            MDCOutHandler sh = new MDCOutHandler();
            List<Handler> handlerChain = new ArrayList<>();
            handlerChain.add(sh);
            ((BindingProvider) port).getBinding().setHandlerChain(handlerChain);

            system.configureSecurity(ClientProxy.getClient(port));

            return port;
        }
    }
}
