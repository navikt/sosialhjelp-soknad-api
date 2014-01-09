package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.modig.cxf.TimeoutFeature;
import no.nav.modig.jaxws.handlers.MDCOutHandler;
import no.nav.sbl.dialogarena.common.timing.TimingFeature;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.feature.LoggingFeature;
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
import static no.nav.modig.security.sts.utility.STSConfigurationUtility.configureStsForExternalSSO;
import static no.nav.modig.security.sts.utility.STSConfigurationUtility.configureStsForSystemUser;
import static org.apache.cxf.common.util.SOAPConstants.MTOM_ENABLED;
import static org.apache.cxf.frontend.ClientProxy.getClient;
import static org.apache.cxf.ws.security.SecurityConstants.MUST_UNDERSTAND;

/**
 * Builder klasse for å lage en porttype.
 *
 * @param <T> klassen det lages for
 */
final class ServiceBuilder<T> {

    public static final int RECEIVE_TIMEOUT = 30000;
    public static final int CONNECTION_TIMEOUT = 10000;
    public Class<T> resultClass;
    JaxWsProxyFactoryBean factoryBean;

    ServiceBuilder(Class<T> resultClass) {
        factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(resultClass);
        this.resultClass = resultClass;
    }

    public ServiceBuilder<T> withExtraClasses(Class[] classes) {
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

    public ServiceBuilder<T> withAddress(String address) {
        factoryBean.setAddress(address);
        return this;
    }

    public ServiceBuilder<T> withLogging() {
        factoryBean.getFeatures().add(new LoggingFeature());
        return this;
    }

    public ServiceBuilder<T> withTiming() {
        factoryBean.getFeatures().add(new TimingFeature(resultClass.getSimpleName()));
        return this;
    }

    public ServiceBuilder<T> withAddressing() {
        factoryBean.getFeatures().add(new WSAddressingFeature());
        return this;
    }

    public ServiceBuilder<T> withTimeout() {
        factoryBean.getFeatures().add(new TimeoutFeature(RECEIVE_TIMEOUT, CONNECTION_TIMEOUT));
        return this;
    }

    public JaxWsProxyFactoryBean get() {
        return factoryBean;
    }

    public ServiceBuilder<T> withProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(MTOM_ENABLED, "true");
        props.put(MUST_UNDERSTAND, false);
        // Denne må settes for å unngå at CXF instansierer EhCache med en non-default konfigurasjon. Denne sørger
        // for at vår konfigurasjon faktisk blir lastet.
        props.put(SecurityConstants.CACHE_CONFIG_FILE, "ehcache.xml");
        factoryBean.setProperties(props);
        return this;
    }

    public PortTypeBuilder<T> build() {
        return new PortTypeBuilder<>(factoryBean.create(resultClass));
    }

    public T portType() {
        T port = factoryBean.create(resultClass);
        return port;
    }

    public ServiceBuilder<T> asStandardService() {
        return this.withAddressing()
                .withLogging()
                .withTimeout()
                .withTiming()
                .withProperties();
    }

    public final class PortTypeBuilder<T> {
        public final T portType;

        private PortTypeBuilder(T factoryBean) {
            this.portType = factoryBean;
        }

        public PortTypeBuilder<T> withUserSecurity() {
            configureStsForExternalSSO(ClientProxy.getClient(portType));
            return this;
        }

        public PortTypeBuilder<T> withSystemSecurity() {
            configureStsForSystemUser(ClientProxy.getClient(portType));
            return this;
        }

        public PortTypeBuilder<T> withMDC() {
            MDCOutHandler sh = new MDCOutHandler();
            List<Handler> handlerChain = new ArrayList<>();
            handlerChain.add(sh);
            ((BindingProvider) portType).getBinding().setHandlerChain(handlerChain);
            return this;
        }

        public PortTypeBuilder<T> withHttpsMock() {
            HTTPConduit httpConduit = (HTTPConduit) getClient(portType).getConduit();
            String property = getProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock");
            if (property != null && property.equals("true")) {
                TLSClientParameters params = new TLSClientParameters();
                params.setDisableCNCheck(true);
                httpConduit.setTlsClientParameters(params);
            } else {
                httpConduit.setTlsClientParameters(new TLSClientParameters());
            }
            return this;
        }

        public T get() {
            return portType;
        }
    }
}
