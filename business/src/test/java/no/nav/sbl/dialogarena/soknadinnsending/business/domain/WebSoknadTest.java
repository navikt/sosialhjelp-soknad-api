package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WebSoknadTest {

	WebSoknad soknad;
	Long soknadId;
	Long faktumId;
	
	@Before
	public void setUp() {
		soknadId = 2l;
		faktumId = 33l;
		soknad = new WebSoknad();
		soknad.setSoknadId(soknadId);
	}
	
	@Test
	public void shouldKunneLageTomSoknad() {
		Assert.assertEquals(0,soknad.antallFakta());
	}
	
	@Test
	public void skalKunneLeggeTilFakta() {
		soknad.leggTilFaktum(new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey("enKey").medValue("enValue"));
		Assert.assertEquals(1,soknad.antallFakta());
	}
}
