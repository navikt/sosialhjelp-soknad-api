package no.nav.sbl.dialogarena.dokumentinnsending.service;


import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPersonnavn;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostadressetyper;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonIntegrationServiceTest {


	@InjectMocks
	private PersonServiceTPS personService = new PersonServiceTPS();
	
	@Mock
	private BrukerprofilPortType brukerProfilMock;

	private static final String IDENT = "12345678910";
	
	@Test
	public void testHentPersonMedKontaktInformasjonSkalReturnerePersonMedKontaktInformasjon() throws Exception{
		XMLHentKontaktinformasjonOgPreferanserRequest request = createMockRequest();
		when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(request)).thenReturn(createMockResponse());

		Person person = personService.hentPerson(IDENT);
		assertThat(person, isA(Person.class));
		
		verify(brukerProfilMock, times(1)).hentKontaktinformasjonOgPreferanser(request);
	}
	
	@Test
	public void testHentPersonSkalIkkeFeileNaarEksterntTjenestekallFeiler() throws Exception{
		XMLHentKontaktinformasjonOgPreferanserRequest request = createMockRequest();
		when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(request)).thenThrow(new ApplicationException("En feil oppstod"));
		
		Person person = personService.hentPerson(IDENT);
		
		verify(brukerProfilMock, times(1)).hentKontaktinformasjonOgPreferanser(request);
		assertFalse(person.harUtenlandsAdresse());
	}

	@Test
	public void testHentPersonSkalIkkeFeileNaarPersonenIkkeBlirFunnet() throws Exception{
		XMLHentKontaktinformasjonOgPreferanserRequest request = createMockRequest();
		when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(request)).
			thenThrow(new HentKontaktinformasjonOgPreferanserPersonIkkeFunnet("Personen ble ikke funnet"));
		
		Person person = personService.hentPerson(IDENT);
		
		verify(brukerProfilMock, times(1)).hentKontaktinformasjonOgPreferanser(request);
		assertFalse(person.harUtenlandsAdresse());
	}
	
	private XMLHentKontaktinformasjonOgPreferanserRequest createMockRequest() {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(IDENT);
    }

    private XMLHentKontaktinformasjonOgPreferanserResponse createMockResponse() {
        XMLHentKontaktinformasjonOgPreferanserResponse response = new XMLHentKontaktinformasjonOgPreferanserResponse();
        response.withPerson(new XMLBruker().withPersonnavn((new XMLPersonnavn().withFornavn("Ola").withEtternavn("Nordmann"))).withGjeldendePostadresseType(lagGjeldendePostAdresse("MIDLERTIDIG_POSTADRESSE_UTLAND")));
        return response;
    }

	private XMLPostadressetyper lagGjeldendePostAdresse(String postAdresseType) {
		return new XMLPostadressetyper().withValue(postAdresseType);
	}
	
}
