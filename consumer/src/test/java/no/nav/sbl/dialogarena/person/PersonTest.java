package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

public class PersonTest {

	@Test
	public void skalSetteSammenForOgEtterNavnPaaRiktigMaateMedNull() {
		Person person = new Person(1l, "11111112345", "Jan", null, "Larsen", "midlertidig", new ArrayList<Adresse>());
		
		Map<String, Object> fakta = person.getFakta();
		
		Faktum sammensattnavn = (Faktum)fakta.get("sammensattnavn");
		Assert.assertEquals("Jan Larsen", sammensattnavn.getValue());
	}

	@Test
	public void skalSetteSammenForOgEtterNavnPaaRiktigMaateMedMellomrom() {
		Person person = new Person(1l, "11111112345", "Jan", "", "Larsen", "midlertidig", new ArrayList<Adresse>());
		
		Map<String, Object> fakta = person.getFakta();
		
		Faktum sammensattnavn = (Faktum)fakta.get("sammensattnavn");
		Assert.assertEquals("Jan Larsen", sammensattnavn.getValue());
	}
	
}
