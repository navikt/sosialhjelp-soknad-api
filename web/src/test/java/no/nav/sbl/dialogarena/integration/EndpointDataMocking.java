package no.nav.sbl.dialogarena.integration;

import no.nav.sbl.dialogarena.config.IntegrationConfig;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;

import javax.xml.datatype.DatatypeFactory;

import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.person.PersonV3Mock.createPersonV3HentPersonRequest;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EndpointDataMocking {

    private static int behandlingsIdCounter = 1;

    public static void setupMockWsEndpointData() throws Exception {
        mockPersonEndpoint();
        mockPersonV3Endpoint();
        mockDkifService();
    }

    static void mockPersonEndpoint() throws Exception {
        PersonPortType personEndpoint = IntegrationConfig.getMocked("personEndpoint");
        HentKjerneinformasjonResponse hentKjerneinformasjonResponse = new HentKjerneinformasjonResponse();

        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(DatatypeFactory.newInstance().newXMLGregorianCalendar());

        Familierelasjoner familierelasjoner = new Familierelasjoner();
        familierelasjoner.setValue("BARN");

        Familierelasjon familierelasjon = new Familierelasjon();
        familierelasjon.setTilRolle(familierelasjoner);

        Person person = new Person();
        Person barn = new Person();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent("04031659235"); // Ikke ekte person
        barn.setIdent(norskIdent);
        barn.setFoedselsdato(foedselsdato);
        familierelasjon.setTilPerson(barn);
        person.getHarFraRolleI().add(familierelasjon);

        person.setFoedselsdato(foedselsdato);
        hentKjerneinformasjonResponse.setPerson(person);

        when(personEndpoint.hentKjerneinformasjon(any())).thenReturn(hentKjerneinformasjonResponse);
    }

    static void mockPersonV3Endpoint() throws Exception {
        PersonV3 mock = IntegrationConfig.getMocked("personV3Endpoint");

        try {
            when(mock.hentPerson(any(HentPersonRequest.class))).thenReturn(createPersonV3HentPersonRequest("12"));
        } catch (HentPersonPersonIkkeFunnet | HentPersonSikkerhetsbegrensning hentPersonPersonIkkeFunnet) {
            hentPersonPersonIkkeFunnet.printStackTrace();
        }

    }

    static void mockDkifService() throws Exception {
        DigitalKontaktinformasjonV1 dkif = IntegrationConfig.getMocked("dkifService");
        when(dkif.hentDigitalKontaktinformasjon(any())).thenReturn(
                new WSHentDigitalKontaktinformasjonResponse()
                        .withDigitalKontaktinformasjon(new WSKontaktinformasjon()
                                .withEpostadresse(new WSEpostadresse().withValue(""))
                        )
        );
    }
}