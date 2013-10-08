package no.nav.sbl.dialogarena;

import java.util.Map;

import no.nav.sbl.dialogarena.websoknad.domain.Faktum;
import no.nav.sbl.dialogarena.websoknad.domain.WebSoknad;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InMemorySoknadInnsendingRepositorytTest {
	
	InMemorySoknadInnsendingRepository repo;
	WebSoknad soknad; 
	Long soknadId;
	
	@Before
	public void setUp( ){
		 repo = new InMemorySoknadInnsendingRepository();
		 soknadId = repo.startSoknad("Dagpenger");
		 soknad = repo.hentSoknad(soknadId);
			
	}
	
	@Test
	public void skalKunneStarteOgHenteSoknad() {
		Assert.assertEquals(soknadId, soknad.getSoknadId());
		Assert.assertEquals(0, soknad.getFakta().size());
	}
	
	@Test
	public void skalKunneLeggeFaktaPaaSoknad() {
		soknad.leggTilFaktum("testFakta", new Faktum(soknadId, "testFakta", "testValue", null));
		
		Map<String, Faktum> fakta = soknad.getFakta();
		Assert.assertEquals(1, fakta.size());
		Assert.assertEquals("testValue", fakta.get("testFakta").getValue());
	}
	
	@Test
	public void skalKunneSletteSoknad() {
		repo.slettSoknad(soknadId);
		soknad = repo.hentSoknad(soknadId);
		
		Assert.assertNull(soknad);
	}
	
}
