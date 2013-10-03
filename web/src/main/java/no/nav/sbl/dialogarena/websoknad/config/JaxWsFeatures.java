package no.nav.sbl.dialogarena.websoknad.config;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

public interface JaxWsFeatures {

    List<Feature> jaxwsFeatures();

    @Configuration
    class Integration implements JaxWsFeatures {

        @Override
        @Bean
        public List<Feature> jaxwsFeatures() {
            List<Feature> features = new ArrayList<>();
            features.add(new LoggingFeature());
            features.add(new WSAddressingFeature());
            features.add(new TimeoutFeature());
            return features;
        }
    }

    @Configuration
    class Mock implements JaxWsFeatures {

        @Override
        @Bean
        public List<Feature> jaxwsFeatures() {
            List<Feature> features = new ArrayList<>();
            features.add(new LoggingFeature());
            features.add(new TimeoutFeature());
            return features;
        }
    }

    class TimeoutFeature extends AbstractFeature {

    	private static final long RECEIVE_TIMEOUT = 30000;
    	private static final long CONNECTION_TIMEOUT = 10000;

    	@Override
        public void initialize(Client client, Bus bus) {
    		Conduit conduit = client.getConduit();
    		if (conduit instanceof HTTPConduit) {
    			HTTPClientPolicy policy = new HTTPClientPolicy();
    			policy.setReceiveTimeout(RECEIVE_TIMEOUT);
    			policy.setConnectionTimeout(CONNECTION_TIMEOUT);
    			HTTPConduit httpConduit = (HTTPConduit) conduit;
    			httpConduit.setClient(policy);
    		}
    		super.initialize(client, bus);
    	}

    }
}
