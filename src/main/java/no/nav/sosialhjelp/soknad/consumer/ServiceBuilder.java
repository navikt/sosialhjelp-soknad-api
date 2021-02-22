package no.nav.sosialhjelp.soknad.consumer;

import no.nav.sbl.dialogarena.common.cxf.LoggingFeatureUtenBinaryOgUtenSamlTokenLogging;
import no.nav.sbl.dialogarena.common.cxf.TimeoutFeature;
import no.nav.sosialhjelp.soknad.consumer.mdc.MDCOutHandler;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.security.SecurityConstants;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;
import static no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.utility.STSConfigurationUtility.configureStsForOnBehalfOfWithJWT;
import static no.nav.sosialhjelp.soknad.consumer.sts.servicegateway.utility.STSConfigurationUtility.configureStsForSystemUserInFSS;
import static org.apache.cxf.frontend.ClientProxy.getClient;
import static org.apache.cxf.ws.security.SecurityConstants.MUST_UNDERSTAND;

/**
 * Builder klasse for å lage en porttype.
 *
 * @param <T> klassen det lages for
 */
public final class ServiceBuilder<T> {

    public static final int RECEIVE_TIMEOUT = 30000;
    public static final int CONNECTION_TIMEOUT = 10000;
    public Class<T> resultClass;
    private JaxWsProxyFactoryBean factoryBean;
    private TimeoutFeature timeoutFeature = null;

    public ServiceBuilder(Class<T> resultClass) {
        factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(resultClass);
        this.resultClass = resultClass;
    }

    public ServiceBuilder<T> withExtraClasses(Class<?>[] classes) {
        factoryBean.getProperties().put("jaxb.additionalContextClasses", classes);
        return this;
    }

    public ServiceBuilder<T> withWsdl(String wsdl) {
        factoryBean.setWsdlURL(wsdl);
        return this;
    }

    public ServiceBuilder<T> withServiceName(QName name) {
        factoryBean.setServiceName(name);
        return this;
    }

    public ServiceBuilder<T> withEndpointName(QName name) {
        factoryBean.setEndpointName(name);
        return this;
    }

    public ServiceBuilder<T> withAddress(String address) {
        factoryBean.setAddress(address);
        return this;
    }

    public ServiceBuilder<T> withLogging() {
        factoryBean.getFeatures().add(new LoggingFeatureUtenBinaryOgUtenSamlTokenLogging());
        return this;
    }

    public ServiceBuilder<T> withAddressing() {
        factoryBean.getFeatures().add(new WSAddressingFeature());
        return this;
    }

    public ServiceBuilder<T> withTimeout() {
        return withTimeout(RECEIVE_TIMEOUT, CONNECTION_TIMEOUT);
    }
    
    public ServiceBuilder<T> withTimeout(int receiveTimeout, int connectionTimeout) {
        timeoutFeature = new TimeoutFeature(receiveTimeout, connectionTimeout);
        return this;
    }

    public ServiceBuilder<T> withProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("mtom-enabled", true);
        props.put(MUST_UNDERSTAND, false);
        // Denne må settes for å unngå at CXF instansierer EhCache med en non-default konfigurasjon. Denne sørger
        // for at vår konfigurasjon faktisk blir lastet.
        props.put(SecurityConstants.CACHE_CONFIG_FILE, "ehcache.xml");
        factoryBean.setProperties(props);
        return this;
    }

    public PortTypeBuilder<T> build() {
        if (timeoutFeature != null) {
            factoryBean.getFeatures().add(timeoutFeature);
        }
        
        return new PortTypeBuilder<>(factoryBean.create(resultClass));
    }

    public T portType() {
        return factoryBean.create(resultClass);
    }

    public ServiceBuilder<T> asStandardService() {
        return this.withAddressing()
                .withLogging()
                .withTimeout()
                .withProperties();
    }

    public final class PortTypeBuilder<U> {
        public final U portType;

        private PortTypeBuilder(U factoryBean) {
            this.portType = factoryBean;
        }

        public PortTypeBuilder<U> withUserSecurity() {
            configureStsForOnBehalfOfWithJWT(ClientProxy.getClient(portType));
            return this;
        }

        public PortTypeBuilder<U> withSystemSecurity() {
            configureStsForSystemUserInFSS(ClientProxy.getClient(portType));
            return this;
        }

        public PortTypeBuilder<U> withMDC() {
            MDCOutHandler sh = new MDCOutHandler();
            @SuppressWarnings("rawtypes")
            List<Handler> handlerChain = new ArrayList<>();
            handlerChain.add(sh);
            ((BindingProvider) portType).getBinding().setHandlerChain(handlerChain);
            return this;
        }

        public PortTypeBuilder<U> withHttpsMock() {
            HTTPConduit httpConduit = (HTTPConduit) getClient(portType).getConduit();
            String property = getProperty("no.nav.sosialhjelp.soknad.sslMock");
            if (property != null && property.equals("true")) {
                TLSClientParameters params = new TLSClientParameters();
                params.setDisableCNCheck(true);
                httpConduit.setTlsClientParameters(params);
            } else {
                httpConduit.setTlsClientParameters(new TLSClientParameters());
            }
            return this;
        }

        public U get() {
            return portType;
        }
    }
}
