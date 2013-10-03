package no.nav.sbl.dialogarena.person;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBostedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGateadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLGyldighetsperiode;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLMidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLNorskIdent;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostnummer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import org.joda.time.DateTime;
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
    private static final String ET_FORNAVN = "Ola";
    private static final String ET_MELLOMNAVN = "Johan";
    private static final String ET_ETTERNAVN = "Normann";
    private static final String FOLKEREGISTRERTADRESSE_VALUE = "BOSTEDSADRESSE";
    private static final String EN_ADRESSE_GATE = "Engesetvegen";
	private static final String EN_ADRESSE_HUSNUMMER = "13";
	private static final String EN_ADRESSE_HUSBOKSTAV = "B";
	private static final String EN_ADRESSE_POSTNUMMER = "0560";
	private static final String EN_ADRESSE_POSTSTED = "Oslo";
	private static final DateTime EN_ANNEN_ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44);
	private static final DateTime EN_ANNEN_ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55);
	private static final String EN_ANNEN_ADRESSE_GATE = "Vegvegen";
	private static final String EN_ANNEN_ADRESSE_HUSNUMMER ="44";
	private static final String EN_ANNEN_ADRESSE_HUSBOKSTAV = "D";
	private static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";
	private static final String EN_POSTBOKS_NAVN = "Postboksstativet";
	private static final String EN_POSTBOKS_NUMMER = "66";

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
    	
    	XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
		response.setPerson(xmlBruker);

		when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(2l,RIKTIG_IDENT);
        Assert.assertNotNull(hentetPerson.getFakta());

        Faktum fnr = (Faktum) hentetPerson.getFakta().get("fnr");
        Assert.assertEquals(RIKTIG_IDENT, fnr.getValue());
        Faktum fornavn = (Faktum) hentetPerson.getFakta().get("fornavn");
        Faktum mellomnavn = (Faktum) hentetPerson.getFakta().get("mellomnavn");
        Faktum etternavnavn = (Faktum) hentetPerson.getFakta().get("etternavn");
        Faktum sammensattnavn = (Faktum) hentetPerson.getFakta().get("sammensattnavn");
        Assert.assertEquals(ET_FORNAVN, fornavn.getValue());
        Assert.assertEquals(ET_MELLOMNAVN, mellomnavn.getValue());
        Assert.assertEquals(ET_ETTERNAVN, etternavnavn.getValue());
		Assert.assertEquals(ET_FORNAVN+" "+ET_MELLOMNAVN+" "+ET_ETTERNAVN, sammensattnavn.getValue());
    }

    @Test
	public void skalStottePersonerUtenMellomnavn() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
    	XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
    	XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
    	
    	XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(false);
    	
		response.setPerson(xmlBruker);

		when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
        Person hentetPerson = service.hentPerson(2l,RIKTIG_IDENT);
        Assert.assertNotNull(hentetPerson.getFakta());

        Faktum mellomnavn = (Faktum) hentetPerson.getFakta().get("mellomnavn");
        Faktum sammensattnavn = (Faktum) hentetPerson.getFakta().get("sammensattnavn");
        Assert.assertEquals("", mellomnavn.getValue());
		Assert.assertEquals(ET_FORNAVN+" "+ET_ETTERNAVN, sammensattnavn.getValue());

	}
    
    @Ignore
	@Test
    public void returnererPersonObjektMedAdresseInformasjon() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
    	XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
    	XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
    	
    	XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
    	
    	XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse();
		xmlBruker.setBostedsadresse(bostedsadresse);
		
		XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();	
		xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
    	
    	XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
    	postadressetyper.setValue(FOLKEREGISTRERTADRESSE_VALUE);
		xmlBruker.setGjeldendePostadresseType(postadressetyper);
		response.setPerson(xmlBruker);
    	
    	when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
    	Person hentetPerson = service.hentPerson(3l, RIKTIG_IDENT);
    	
    	@SuppressWarnings("unchecked")
    	List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get("adresser");
    	Assert.assertNotNull(adresseliste);
    	Assert.assertEquals(EN_ADRESSE_GATE, adresseliste.get(0).getGatenavn());
    	Assert.assertEquals(EN_ADRESSE_HUSNUMMER, adresseliste.get(0).getHusnummer());
    	Assert.assertEquals(EN_ADRESSE_HUSBOKSTAV, adresseliste.get(0).getHusbokstav());
    	Assert.assertEquals(EN_ADRESSE_POSTNUMMER, adresseliste.get(0).getPostnummer());
    	Assert.assertEquals(EN_ADRESSE_POSTSTED, adresseliste.get(0).getPoststed());
    	Assert.assertTrue(adresseliste.size() > 1);
		Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(1).getGyldigFra());
		Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(1).getGyldigTil());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_GATE, adresseliste.get(1).getGatenavn());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_HUSNUMMER, adresseliste.get(1).getHusnummer());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_HUSBOKSTAV, adresseliste.get(1).getHusbokstav());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_POSTNUMMER, adresseliste.get(1).getPostnummer());
    	
    }

	private XMLMidlertidigPostadresseNorge generateMidlertidigAdresseNorge() {
		//TODO: Inn med C/O,
		// C/O?
		XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = new XMLMidlertidigPostadresseNorge();
		XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode();
		xmlMidlertidigNorge.setPostleveringsPeriode(xmlGyldighetsperiode);
		XMLGateadresse xmlgateadresse = new XMLGateadresse();
		xmlgateadresse.setGatenavn(EN_ANNEN_ADRESSE_GATE);
		xmlgateadresse.setHusnummer(new BigInteger(EN_ANNEN_ADRESSE_HUSNUMMER));
		xmlgateadresse.setHusbokstav(EN_ANNEN_ADRESSE_HUSBOKSTAV);
		XMLPostnummer xmlpostnummer = new XMLPostnummer();
		xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
		xmlgateadresse.setPoststed(xmlpostnummer);
		xmlMidlertidigNorge.setStrukturertAdresse(xmlgateadresse);
		return xmlMidlertidigNorge;
	}
	
	private XMLMidlertidigPostadresseNorge generateMidlertidigPostboksAdresseNorge() {
		//TODO: Inn med C/O,
		// c/o, postboks, navn, postnummer, postsed
		XMLMidlertidigPostadresseNorge xmlMidlertidigPostboksNorge = new XMLMidlertidigPostadresseNorge();
		XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode();
		xmlMidlertidigPostboksNorge.setPostleveringsPeriode(xmlGyldighetsperiode);
		
		XMLPostboksadresseNorsk xmlpostboksadresse = new XMLPostboksadresseNorsk();
		xmlpostboksadresse.setPostboksanlegg(EN_POSTBOKS_NAVN);
		xmlpostboksadresse.setPostboksnummer(EN_POSTBOKS_NUMMER);
		XMLPostnummer xmlpostnummer = new XMLPostnummer();
		xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
		xmlpostboksadresse.setPoststed(xmlpostnummer);
		xmlMidlertidigPostboksNorge.setStrukturertAdresse(xmlpostboksadresse);
		return xmlMidlertidigPostboksNorge;
	}

	private XMLGyldighetsperiode generateGyldighetsperiode() {
		XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
		xmlGyldighetsperiode.setFom(EN_ANNEN_ADRESSE_GYLDIG_FRA);
		xmlGyldighetsperiode.setTom(EN_ANNEN_ADRESSE_GYLDIG_TIL);
		return xmlGyldighetsperiode;
	}


    private XMLBruker genererXmlBrukerMedGyldigIdentOgNavn(boolean medMellomnavn) {
    	XMLBruker xmlBruker = new XMLBruker();
    	XMLPersonnavn personNavn = new XMLPersonnavn();
    	personNavn.setFornavn(ET_FORNAVN);
    	if(medMellomnavn) {
    		personNavn.setMellomnavn(ET_MELLOMNAVN);
    		personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " " +ET_ETTERNAVN);
    	} else {
    		personNavn.setMellomnavn("");
    		personNavn.setSammensattNavn(ET_FORNAVN + " " +ET_ETTERNAVN);
    	}
    	personNavn.setEtternavn(ET_ETTERNAVN);
		xmlBruker.setPersonnavn(personNavn);
    	XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
    	xmlNorskIdent.setIdent(RIKTIG_IDENT);
		xmlBruker.setIdent(xmlNorskIdent);
		return xmlBruker;
	}

	private XMLBostedsadresse genererXMLFolkeregistrertAdresse() {
		XMLBostedsadresse bostedsadresse = new XMLBostedsadresse();
    	XMLGateadresse gateadresse = new XMLGateadresse();
    	gateadresse.setGatenavn(EN_ADRESSE_GATE);
    	gateadresse.setHusnummer(new BigInteger(EN_ADRESSE_HUSNUMMER));
    	gateadresse.setHusbokstav(EN_ADRESSE_HUSBOKSTAV);
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
	

	@Test
	public void skalStotteMidlertidigPostboksAdresseNorge() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
		XMLHentKontaktinformasjonOgPreferanserRequest request = hentRequestMedGyldigIdent();
    	XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
    	
    	XMLBruker xmlBruker = genererXmlBrukerMedGyldigIdentOgNavn(true);
    	
    	XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge();
		xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
		
		response.setPerson(xmlBruker);
    	
    	when(brukerprofilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(response);
    	Person hentetPerson = service.hentPerson(4l, RIKTIG_IDENT);
    	
    	@SuppressWarnings("unchecked")
    	List<Adresse> adresseliste = (List<Adresse>) hentetPerson.getFakta().get("adresser");
    	Assert.assertNotNull(adresseliste);
    	Assert.assertEquals(EN_POSTBOKS_NAVN, adresseliste.get(0).getPostboksNavn());
    	Assert.assertEquals(EN_POSTBOKS_NUMMER, adresseliste.get(0).getPostboksNummer());
    	Assert.assertEquals(EN_ANNEN_ADRESSE_POSTNUMMER, adresseliste.get(0).getPostnummer());
		Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_FRA, adresseliste.get(0).getGyldigFra());
		Assert.assertEquals(EN_ANNEN_ADRESSE_GYLDIG_TIL, adresseliste.get(0).getGyldigTil());
	}
	
	@Ignore
	@Test
	public void skalStotteMidlertidigOmrodeAdresseNorge() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
		//Not implemented - fritekst, postnummer og poststed
		//Hva er dette egentlig?
		Assert.assertTrue(false);
	}
	
	@Ignore
	@Test
	public void skalStotteMidlertidigUtenlandskMidlertidigAdresse() {
		//Not implemented - adresse og land
		Assert.assertTrue(false);
	}
	
	
	
	@Ignore
	@Test
	public void skalStottePoststed() {
		//Not implemented
		Assert.assertTrue(false);
	}
    
}
