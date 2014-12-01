package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.arena.tjenester.person.v1.PersonInfoServiceSoap;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.cxf.TimeoutFeature;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoConnector;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.kodeverk.KodeverkMock;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder.CONNECTION_TIMEOUT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder.RECEIVE_TIMEOUT;

@Configuration
@EnableCaching
@Import({
        ConsumerConfig.WsServices.class,
        FillagerConnector.class,
        HenvendelseConnector.class,
        PersonConnector.class,
        PersonInfoConnector.class
})
public class ConsumerConfig {
    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Import({
            SendSoknadWSConfig.class,
            FilLagerWSConfig.class,
            PersonInfoWSConfig.class,
            BrukerProfilWSConfig.class,
            KodeverkWSConfig.class,
            PersonWSConfig.class})
    public static class WsServices {

    }

    @Configuration
    public static class SendSoknadWSConfig {
        @Value("${soknad.webservice.henvendelse.sendsoknadservice.url}")
        private String soknadServiceEndpoint;

        private ServiceBuilder<SendSoknadPortType>.PortTypeBuilder<SendSoknadPortType> factory() {
            return new ServiceBuilder<>(SendSoknadPortType.class)
                    .asStandardService()
                    .withAddress(soknadServiceEndpoint)
                    .withWsdl("classpath:SendSoknad.wsdl")
                            //.withServiceName(new QName("http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", "SendSoknadPortType"))
                    .withExtraClasses(new Class[]{XMLMetadataListe.class, WSSoknadsdata.class, WSStartSoknadRequest.class, XMLMetadata.class, XMLVedlegg.class, XMLHovedskjema.class})
                    .build()
                    .withHttpsMock()
                    .withMDC();
        }

        @Bean
        public SendSoknadPortType sendSoknadService() {
            return factory().withUserSecurity().get();
        }

        @Bean
        public SendSoknadPortType sendSoknadSelftest() {
            return factory().withSystemSecurity().get();
        }
    }

    @Configuration
    public static class FilLagerWSConfig {
        @Value("${soknad.webservice.henvendelse.fillager.url}")
        private String serviceEndpoint;

        private ServiceBuilder<FilLagerPortType>.PortTypeBuilder<FilLagerPortType> factory() {
            return new ServiceBuilder<>(FilLagerPortType.class)
                    .asStandardService()
                    .withAddress(serviceEndpoint)
                    .withWsdl("classpath:FilLager.wsdl")
                    .build()
                    .withHttpsMock();
        }

        @Bean
        public FilLagerPortType fillagerService() {
            return factory().withMDC().withUserSecurity().get();
        }

        @Bean
        public FilLagerPortType fillagerServiceSelftest() {
            return factory().withSystemSecurity().get();
        }
    }

    @Configuration
    public static class PersonInfoWSConfig {
        @Value("${soknad.webservice.arena.personinfo.url}")
        private String endpoint;

        @Bean
        public PersonInfoServiceSoap personInfoServiceSoap() {
            JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
            factoryBean.setServiceClass(PersonInfoServiceSoap.class);
            factoryBean.setAddress(endpoint);

            Map<String, Object> map = new HashMap<>();
            map.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
            map.put(WSHandlerConstants.PASSWORD_TYPE, "PasswordText");
            map.put(WSHandlerConstants.USER, getProperty("arena.personInfoService.username"));
            CallbackHandler passwordCallbackHandler = new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
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

    }

    @Configuration
    public static class PersonWSConfig {

        @Value("${soknad.webservice.person.personservice.url}")
        private String personEndpoint;

        private ServiceBuilder<PersonPortType>.PortTypeBuilder<PersonPortType> factory() {
            return new ServiceBuilder<>(PersonPortType.class)
                    .asStandardService()
                    .withAddress(personEndpoint)
                    .withWsdl("classpath:/wsdl/no/nav/tjeneste/virksomhet/person/v1/Person.wsdl")
                    .build()
                    .withHttpsMock()
                    .withMDC();
        }

        @Bean
        public PersonPortType personService() {
            return factory().withUserSecurity().get();
        }

        @Bean
        public PersonPortType personServiceSelftest() {
            return factory().withSystemSecurity().get();
        }

    }

    @Configuration
    public static class KodeverkWSConfig {

        public static final String KODEVERK_KEY = "start.kodeverk.withmock";

        @Value("${sendsoknad.webservice.kodeverk.url}")
        private String kodeverkEndPoint;

        private ServiceBuilder<KodeverkPortType>.PortTypeBuilder<KodeverkPortType> factory() {
            return new ServiceBuilder<>(KodeverkPortType.class)
                    .asStandardService()
                    .withAddress(kodeverkEndPoint)
                    .withWsdl("classpath:kodeverk/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl")
                    .build()
                    .withHttpsMock();
        }

        @Bean
        public KodeverkPortType kodeverkService() {
            KodeverkPortType prod = factory().withSystemSecurity().get();
            KodeverkPortType mock = new KodeverkMock().kodeverkMock();
            if (true) {
                return mock;
            }
            return prod;
        }

        @Bean
        public KodeverkPortType kodeverkServiceSelftest() {
            return factory().withSystemSecurity().get();
        }
    }

    @Configuration
    public static class BrukerProfilWSConfig {

        @Value("${soknad.webservice.brukerprofil.brukerprofilservice.url}")
        private String brukerProfilEndpoint;

        private ServiceBuilder<BrukerprofilPortType>.PortTypeBuilder<BrukerprofilPortType> factory() {
            return new ServiceBuilder<>(BrukerprofilPortType.class)
                    .asStandardService()
                    .withAddress(brukerProfilEndpoint)
                    .withWsdl("classpath:brukerprofil/no/nav/tjeneste/virksomhet/brukerprofil/v1/Brukerprofil.wsdl")
                    .build()
                    .withHttpsMock()
                    .withMDC();
        }

        @Bean
        public BrukerprofilPortType brukerProfilService() {
            return factory().withUserSecurity().get();
        }

        @Bean
        public BrukerprofilPortType brukerProfilSelftest() {
            return factory().withSystemSecurity().get();
        }
    }
}
