package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.aetat.arena.fodselsnr.Fodselsnr;
import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.sbl.dialogarena.common.cxf.TimeoutFeature;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.personinfo.PersonInfoMock;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;
import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createMetricsProxyWithInstanceSwitcher;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder.CONNECTION_TIMEOUT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder.RECEIVE_TIMEOUT;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
public class PersonInfoWSConfig {

    public static final String PERSONINFO_KEY = "start.personinfo.withmock";

    @Value("${soknad.webservice.arena.personinfo.url}")
    private String endpoint;

    @Bean
    public PersonInfoServiceSoap personInfoEndpoint() {
        PersonInfoServiceSoap mock = new PersonInfoMock().personInfoMock();
        PersonInfoServiceSoap prod = opprettPersonInfoEndpoint();
        return createMetricsProxyWithInstanceSwitcher("Personinfo", prod, mock, PERSONINFO_KEY, PersonInfoServiceSoap.class);
    }

    private PersonInfoServiceSoap opprettPersonInfoEndpoint() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(PersonInfoServiceSoap.class);
        factoryBean.setAddress(endpoint);

        Map<String, Object> map = new HashMap<>();
        map.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        map.put(WSHandlerConstants.PASSWORD_TYPE, "PasswordText");
        map.put(WSHandlerConstants.USER, getProperty("arena.personInfoService.username"));
        CallbackHandler passwordCallbackHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) {
                WSPasswordCallback callback = (WSPasswordCallback) callbacks[0];
                callback.setPassword(getProperty("arena.personInfoService.password"));
            }
        };
        map.put(WSHandlerConstants.PW_CALLBACK_REF, passwordCallbackHandler);
        factoryBean.getOutInterceptors().add(new WSS4JOutInterceptor(map));

        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getFeatures().add(new TimeoutFeature(RECEIVE_TIMEOUT, CONNECTION_TIMEOUT));
        return factoryBean.create(PersonInfoServiceSoap.class);
    }

    @Bean
    public Pingable personInfoPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                Fodselsnr fodselsnr = new Fodselsnr().withFodselsnummer("01034128789");
                PingMetadata metadata = new PingMetadata(endpoint,"ARENA - Personinfo (Status på personen)", false);
                try {
                    personInfoEndpoint().hentPersonStatus(fodselsnr);
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
