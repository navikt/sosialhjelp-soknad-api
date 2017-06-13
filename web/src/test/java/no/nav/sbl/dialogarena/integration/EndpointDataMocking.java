package no.nav.sbl.dialogarena.integration;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontonummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;

public class EndpointDataMocking {

    private static int behandlingsIdCounter = 1;

    static void setupMockWsEndpointData() throws Exception {
        mockSendSoknadEndpoint();
        mockBrukerProfilEndpoint();
        mockPersonEndpoint();
        mockDkifService();
        mockArbeidsForholdService();
    }

    static void mockSendSoknadEndpoint() {
        SendSoknadPortType soknad = IntegrationConfig.getMocked("sendSoknadEndpoint");
        Mockito.when(soknad.startSoknad(any(WSStartSoknadRequest.class)))
                .then(invocationOnMock -> new WSBehandlingsId().withBehandlingsId("TEST" + behandlingsIdCounter++));
    }

    static void mockBrukerProfilEndpoint() throws Exception {
        BrukerprofilPortType brukerProfil = IntegrationConfig.getMocked("brukerProfilEndpoint");
        Mockito.when(brukerProfil.hentKontaktinformasjonOgPreferanser(any())).thenReturn(
                new XMLHentKontaktinformasjonOgPreferanserResponse().withPerson(
                        new XMLBruker()
                                .withBankkonto(new XMLBankkontoNorge()
                                        .withBankkonto(new XMLBankkontonummer().withBankkontonummer("65294512345"))
                                )
                                .withIdent(new XMLNorskIdent().withIdent("***REMOVED***"))
                )
        );
    }

    static void mockPersonEndpoint() throws Exception {
        PersonPortType personEndpoint = IntegrationConfig.getMocked("personEndpoint");
        HentKjerneinformasjonResponse hentKjerneinformasjonResponse = new HentKjerneinformasjonResponse();

        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(new XMLGregorianCalendarImpl());

        Familierelasjoner familierelasjoner = new Familierelasjoner();
        familierelasjoner.setValue("BARN");

        Familierelasjon familierelasjon = new Familierelasjon();
        familierelasjon.setTilRolle(familierelasjoner);

        Person person = new Person();
        Person barn = new Person();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent("***REMOVED***");
        barn.setIdent(norskIdent);
        barn.setFoedselsdato(foedselsdato);
        familierelasjon.setTilPerson(barn);
        person.getHarFraRolleI().add(familierelasjon);

        person.setFoedselsdato(foedselsdato);
        hentKjerneinformasjonResponse.setPerson(person);

        Mockito.when(personEndpoint.hentKjerneinformasjon(any())).thenReturn(hentKjerneinformasjonResponse);
    }

    static void mockDkifService() throws Exception {
        DigitalKontaktinformasjonV1 dkif = IntegrationConfig.getMocked("dkifService");
        Mockito.when(dkif.hentDigitalKontaktinformasjon(any())).thenReturn(
                new WSHentDigitalKontaktinformasjonResponse()
                        .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                                .withEpostadresse(new WSEpostadresse().withValue(""))
                        )
        );
    }

    static void mockArbeidsForholdService() throws Exception {
        ArbeidsforholdV3 arbeidEndpont = IntegrationConfig.getMocked("arbeidEndpoint");
        Mockito.when(arbeidEndpont.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class)))
                .thenReturn(new FinnArbeidsforholdPrArbeidstakerResponse());
    }
}