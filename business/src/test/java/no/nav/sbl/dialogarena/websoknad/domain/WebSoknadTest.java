package no.nav.sbl.dialogarena.websoknad.domain;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

public class WebSoknadTest {

	WebSoknad soknad;
	Long soknadId;
	
	@Before
	public void setUp() {
		soknadId = 2l;
		soknad = new WebSoknad();
		soknad.setSoknadId(soknadId);
	}
	
	@Test
	public void shouldKunneLageTomSoknad() {
		Assert.assertEquals(0,soknad.antallFakta());
	}
	
	@Test
	public void skalKunneLeggeTilFakta() {
		soknad.leggTilFaktum("enKey", new Faktum(soknadId, "enKey", "enValue", null));
		Assert.assertEquals(1,soknad.antallFakta());
	}
}
