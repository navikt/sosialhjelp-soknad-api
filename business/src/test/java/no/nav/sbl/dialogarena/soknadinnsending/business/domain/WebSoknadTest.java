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
		soknad.leggTilFaktum("enKey", new Faktum(soknadId, faktumId, "enKey", "enValue", null));
		Assert.assertEquals(1,soknad.antallFakta());
	}
	
	@Test
	public void skalKunneLeggeTilFaktumId() {
		Faktum faktum = new Faktum(1l, 5l, "barneliste", "{\"fnr\":\"06025800174\", \"valgt\":true}");
		String result = soknad.hentBarnJsonMedFaktumId(faktum);
		Assert.assertEquals("{\"fnr\":\"06025800174\", \"valgt\":true, \"faktumId\": 5}", result);
	}
	
	@Test
	public void skalIkkeLeggeTilFaktumId2Ganger() {
		Faktum faktum = new Faktum(1l, 5l, "barneliste", "{\"fnr\":\"06025800174\", \"valgt\":true}");
		String runde1 = soknad.hentBarnJsonMedFaktumId(faktum);
		faktum.setValue(runde1);
		
		String runde2 = soknad.hentBarnJsonMedFaktumId(faktum);
		Assert.assertEquals("{\"fnr\":\"06025800174\", \"valgt\":true, \"faktumId\": 5}", runde2);
	}
	
}
