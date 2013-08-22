package no.nav.sbl.dialogarena.dokumentinnsending.transform;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import no.nav.sbl.dialogarena.dokumentinnsending.builder.PersonFactory;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Person;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import org.junit.Before;
import org.junit.Test;

public class PersonTransformTest {

	private PersonTransform personTransform;
	private XMLHentKontaktinformasjonOgPreferanserResponse response;
	@Before
	public void beforeEachTest() {
		personTransform = new PersonTransform();
	}
	
	@Test
	public void personMedMidlertidigAdressHarAdresseIUtlandet() {
		String ident = "10109012345";
		response = PersonFactory.lagPersonMedMidlertidigUtlandsAdresse(ident);
		Person person = personTransform.mapToPerson(response);
		assertTrue(person.harUtenlandsAdresse());
	}
	
	@Test
	public void personMedNorskMidlertidigAdresseHarIkkeAdresseIUtlandet() {
		String ident = "12127612345";
		response = PersonFactory.lagPersonMedNorskMidlertidigAdresse(ident);
		Person person = personTransform.mapToPerson(response);
		assertFalse(person.harUtenlandsAdresse());
	}
	
	@Test
	public void personMedFastUtenlandskStedsadresseHarAdresseIUtlandet() {
		String ident = "15107554321";
		response = PersonFactory.lagPersonMedFastUtenlandskAdresse(ident);
		Person person = personTransform.mapToPerson(response);
		assertTrue(person.harUtenlandsAdresse());
	}
	
}
