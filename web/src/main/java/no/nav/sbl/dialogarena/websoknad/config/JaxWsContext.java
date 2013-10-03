package no.nav.sbl.dialogarena.websoknad.config;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.util.HashMap;
import java.util.Map;

import no.nav.sbl.dialogarena.common.PingExecutor;
import no.nav.sbl.dialogarena.common.timing.TimingFeature;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

@Configuration
public class JaxWsContext {

	@Autowired
	private Environment env;

    @Autowired
    private JaxWsFeatures jaxwsFeatures;

	@Bean
    public KodeverkPortType kodeverkSystemUser() {
        return kodeverkPortTypeFactory().create(KodeverkPortType.class);
    }

    @Bean
    public Pingable kodeverkPortTypePing() {
        final KodeverkPortType portType = kodeverkSystemUser();
        return new PingExecutor("KODEVERK") {
            @Override
            protected void pingOperation() {
                portType.ping();
            }
        };
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public JaxWsProxyFactoryBean kodeverkPortTypeFactory() {
        JaxWsProxyFactoryBean jaxwsClient = commonJaxWsConfig();
        //setter mustunderstand i header slik at tjenester som ikke forstår sikkerhetsheader ikke skal avvise requester
        jaxwsClient.getProperties().put(SecurityConstants.MUST_UNDERSTAND, false);
        jaxwsClient.getFeatures().add(new TimingFeature(KodeverkPortType.class.getSimpleName()));
        jaxwsClient.setServiceClass(KodeverkPortType.class);
        jaxwsClient.setAddress(env.getRequiredProperty("sendsoknad.webservice.kodeverk.url"));
        jaxwsClient.setWsdlURL(classpathUrl("kodeverk/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl"));
        return jaxwsClient;
    }





    @Bean
	@Scope(SCOPE_PROTOTYPE)
	public JaxWsProxyFactoryBean commonJaxWsConfig() {
		JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
		Map<String, Object> properties = new HashMap<>();
		properties.put("schema-validation-enabled", true);
        // Denne må settes for å unngå at CXF instansierer EhCache med en non-default konfigurasjon. Denne sørger
        // for at vår konfigurasjon faktisk blir lastet.
		properties.put(SecurityConstants.CACHE_CONFIG_FILE, "ehcache.xml");
		factoryBean.setProperties(properties);
        factoryBean.getFeatures().addAll(jaxwsFeatures.jaxwsFeatures());
		return factoryBean;
	}



    private String classpathUrl(String classpathLocation) {
        if (getClass().getClassLoader().getResource(classpathLocation) == null) {
            throw new RuntimeException(classpathLocation + " does not exist on classpath!");
        }
        return "classpath:" + classpathLocation;
    }

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        System.setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

}
