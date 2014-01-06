package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.xml.namespace.QName;

import static java.lang.System.setProperty;

@Configuration
@ComponentScan(excludeFilters = @ComponentScan.Filter(Configuration.class))
@EnableCaching
@Import(ConsumerConfig.WsServices.class)
public class ConsumerConfig {
    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Import({
            AktorWsConfig.class,
            SendSoknadWSConfig.class,
            FilLagerWSConfig.class,
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
                    .withHttpsMock()
                    .withMDC();
        }

        @Bean
        public FilLagerPortType fillagerService() {
            return factory().withUserSecurity().get();
        }

        @Bean
        public FilLagerPortType fillagerServiceSelftest() {
            return factory().withSystemSecurity().get();
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
    public static class AktorWsConfig {
        @Value("${soknad.webservice.aktor.aktorservice.url:}")
        private String aktorServiceEndpoint;

        private ServiceBuilder<AktoerPortType>.PortTypeBuilder<AktoerPortType> factory() {
            return new ServiceBuilder<>(AktoerPortType.class)
                    .asStandardService()
                    .withAddress(aktorServiceEndpoint)
                    .withWsdl("classpath:wsdl/no/nav/tjeneste/virksomhet/aktoer/v1/Aktoer.wsdl")
                    .withServiceName(new QName("http://nav.no/tjeneste/virksomhet/aktoer/v1/", "AktoerPortType"))
                    .build()
                    .withHttpsMock()
                    .withMDC();
        }

        @Bean
        public AktoerPortType aktorPortType() {
            return factory().withUserSecurity().get();
        }

        @Bean
        public AktoerPortType aktorSelftestPortType() {
            return factory().withSystemSecurity().get();
        }
    }

    @Configuration
    public static class KodeverkWSConfig {
        @Value("${sendsoknad.webservice.kodeverk.url}")
        private String kodeverkEndPoint;

        private ServiceBuilder<KodeverkPortType>.PortTypeBuilder<KodeverkPortType> factory() {
            return new ServiceBuilder<>(KodeverkPortType.class)
                    .asStandardService()
                    .withAddress(kodeverkEndPoint)
                    .withWsdl("classpath:kodeverk/no/nav/tjeneste/virksomhet/kodeverk/v2/Kodeverk.wsdl")
                    .build()
                    .withHttpsMock()
                    .withMDC();
        }

        @Bean
        public KodeverkPortType kodeverkService() {
            return factory().withUserSecurity().get();
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
