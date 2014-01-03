package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.aktoer.v1.AktoerPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import org.hamcrest.CustomMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@ComponentScan
public class MockConsumerConfig {

    @Configuration
    public static class SendSoknadWSConfig {
        private int id = 0;

        @Bean
        public SendSoknadPortType sendSoknadService() {
            SendSoknadPortType mock = mock(SendSoknadPortType.class);
            when(mock.startSoknad(any(WSStartSoknadRequest.class))).thenReturn(new WSBehandlingsId().withBehandlingsId("ID" + id++));
            return mock;
        }

        @Bean
        public SendSoknadPortType sendSoknadSelftest() {
            return sendSoknadService();
        }
    }

    @Configuration
    public static class FilLagerWSConfig {

        @Bean
        public FilLagerPortType fillagerService() {
            return mock(FilLagerPortType.class);
        }

        @Bean
        public FilLagerPortType fillagerServiceSelftest() {
            return fillagerService();
        }
    }

    @Configuration
    public static class PersonWSConfig {

        @Bean
        public PersonPortType personService() {
            return mock(PersonPortType.class);
        }

        @Bean
        public PersonPortType personServiceSelftest() {
            return personService();
        }

    }

    @Configuration
    public static class AktorWsConfig {

        @Bean
        public AktoerPortType aktorPortType() {
            return mock(AktoerPortType.class);
        }

        @Bean
        public AktoerPortType aktorSelftestPortType() {
            return aktorPortType();
        }
    }

    @Configuration
    public static class KodeverkWSConfig {

        private static XMLHentKodeverkResponse landkodeKodeverkResponse() {
            XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge"));
            XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige"));
            XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania"));
            XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark"));

            return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, sverige, albania, danmark));
        }

        @Bean
        public KodeverkPortType kodeverkService() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            KodeverkPortType mock = mock(KodeverkPortType.class);
            when(mock.hentKodeverk(argThat(new CustomMatcher<XMLHentKodeverkRequest>("sjekk om kodeverk matcher") {
                @Override
                public boolean matches(Object item) {
                    XMLHentKodeverkRequest kodeverkRequest = (XMLHentKodeverkRequest) item;
                    return kodeverkRequest.getNavn().equals("Landkoder");
                }
            }))).thenReturn(postnummerKodeverkResponse());
            return mock;
        }

        @Bean
        public KodeverkPortType kodeverkServiceSelftest() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
            return kodeverkService();
        }

        private XMLHentKodeverkResponse postnummerKodeverkResponse() {
            XMLKode kode = new XMLKode().withNavn("0565").withTerm(new XMLTerm().withNavn("Oslo"));
            return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Kommuner").withKode(kode));
        }

    }

    @Configuration
    public static class BrukerProfilWSConfig {


        @Bean
        public BrukerprofilPortType brukerProfilService() {
            return mock(BrukerprofilPortType.class);
        }

        @Bean
        public BrukerprofilPortType brukerProfilSelftest() {
            return brukerProfilService();
        }
    }
}
