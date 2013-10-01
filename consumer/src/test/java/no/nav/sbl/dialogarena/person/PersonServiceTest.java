package no.nav.sbl.dialogarena.person;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;

import no.nav.sbl.dialogarena.adresse.Adressetype;
import no.nav.sbl.dialogarena.adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonServiceTest {

    private static final String RIKTIG_IDENT = "12345";
    private static final String FEIL_IDENT = "54321";
    private static final String ET_NAVN = "Ola Nordmann";
    private static final String FOLKEREGISTRERTADRESSE_VALUE = "BOSTEDSADRESSE";
    private static final String EN_ADRESSE_GATE = "Engesetvegen";
	private static final String EN_ADRESSE_HUSNUMMER = "13";
	private static final String EN_ADRESSE_POSTNUMMER = "6150";
	private static final String EN_ANNEN_ADRESSE_GATE = "Vegvegen";
	private static final String EN_ANNEN_ADRESSE_HUSNUMMER ="44";
	private static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";

    @InjectMocks
    private PersonServiceTPS service;

    @Mock
    private BrukerprofilPortType brukerprofilMock;

    @SuppressWarnings("unchecked")
	@Test
    public void returnerPersonUtenDataHvisPersonenSomReturneresHarFeilIdent() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
    	XMLHentKontaktinformasjonOgPreferanserRequest request = new XMLHentKontaktinformasjonOgPreferanserRequest();
    	request.setIdent(FEIL_IDENT);


        when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenThrow(HentKontaktinformasjonOgPreferanserPersonIkkeFunnet.class);
        Person person = service.hentPerson(1l, FEIL_IDENT);

        Assert.assertNotNull(person);
    }

    @Test
    public void returnererPersonObjektDersomPersonenSomReturneresHarRiktigIdent() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
    	XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();


    	XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
    	
    	XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn();
		response.setPerson(xmlBruker);
		

		when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(2l,RIKTIG_IDENT);
        Assert.assertNotNull(hentetPerson.getFakta());

        Faktum fnr = (Faktum) hentetPerson.getFakta().get("fnr");
        Assert.assertEquals(RIKTIG_IDENT, fnr.getValue());
        Faktum sammensattnavn = (Faktum) hentetPerson.getFakta().get("sammensattnavn");
		Assert.assertEquals(ET_NAVN, sammensattnavn.getValue());
    }
    
    private XMLBruker genererXmlBrukerMedGyldigIdentOgNavn() {
    	XMLBruker xmlBruker = new XMLBruker();
    	XMLPersonnavn personNavn = new XMLPersonnavn();
    	personNavn.setSammensattNavn(ET_NAVN);
		xmlBruker.setPersonnavn(personNavn);
    	XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
    	xmlNorskIdent.setIdent(RIKTIG_IDENT);
		xmlBruker.setIdent(xmlNorskIdent);
		return xmlBruker;
	}

	@Test
    public void returnererPersonObjektMedAdresseInformasjon() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
    	XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
    	XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
    	
    	XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn();
    	
    	XMLBostedsadresse bostedsadresse = genererXMLBostedsAdresse();
		xmlBruker.setBostedsadresse(bostedsadresse);
		
		XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = new XMLMidlertidigPostadresseNorge();
		XMLGateadresse xmlgateadresse = new XMLGateadresse();
		xmlgateadresse.setGatenavn(EN_ANNEN_ADRESSE_GATE);
		xmlgateadresse.setGatenummer(new BigInteger(EN_ANNEN_ADRESSE_HUSNUMMER));
		XMLPostnummer xmlpostnummer = new XMLPostnummer();
		xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
		xmlgateadresse.setPoststed(xmlpostnummer);
		xmlMidlertidigNorge.setStrukturertAdresse(xmlgateadresse);
		
		xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
    	
    	XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
    	postadressetyper.setValue(FOLKEREGISTRERTADRESSE_VALUE);
		xmlBruker.setGjeldendePostadresseType(postadressetyper);
		response.setPerson(xmlBruker);
    	
    	when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
    	Person hentetPerson = service.hentPerson(3l, RIKTIG_IDENT);
    	
    	@SuppressWarnings("unchecked")
    	List<PersonAdresse> adresseliste = (List<PersonAdresse>) hentetPerson.getFakta().get("adresser");
    	Assert.assertNotNull(adresseliste);
    	Assert.assertEquals(EN_ADRESSE_GATE, adresseliste.get(0).getGatenavn());
    	Assert.assertEquals(EN_ADRESSE_HUSNUMMER, adresseliste.get(0).getHusnummer());
    	Assert.assertEquals(EN_ADRESSE_POSTNUMMER, adresseliste.get(0).getPostnummer());
    	Assert.assertTrue(adresseliste.size() > 1);
    	Assert.assertEquals(EN_ANNEN_ADRESSE_GATE, adresseliste.get(1).getGatenavn());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_HUSNUMMER, adresseliste.get(1).getHusnummer());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_POSTNUMMER, adresseliste.get(1).getPostnummer());
    	
    }
	
	@Ignore
	@Test
	public void skalStotteUtenlandskMidlertidigAdresse() {
		//Not implemented
		Assert.assertTrue(false);
	}

	private XMLBostedsadresse genererXMLBostedsAdresse() {
		XMLBostedsadresse bostedsadresse = new XMLBostedsadresse();
    	XMLGateadresse gateadresse = new XMLGateadresse();
    	gateadresse.setGatenavn(EN_ADRESSE_GATE);
    	gateadresse.setGatenummer(new BigInteger(EN_ADRESSE_HUSNUMMER));
		XMLPostnummer xmlpostnummer = new XMLPostnummer();
		xmlpostnummer.setValue(EN_ADRESSE_POSTNUMMER);
		gateadresse.setPoststed(xmlpostnummer);
		bostedsadresse.setStrukturertAdresse(gateadresse);
		return bostedsadresse;
	}

	private XMLHentKontaktinformasjonOgPreferanserRequest hentRequestMedGyldigIdent() {
		XMLHentKontaktinformasjonOgPreferanserRequest request = new XMLHentKontaktinformasjonOgPreferanserRequest();
    	request.setIdent(RIKTIG_IDENT);
		return request;
	}
    
}
