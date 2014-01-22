package no.nav.sbl.dialogarena.websoknad.servlet;

import org.junit.Assert;
import org.junit.Test;

public class FortsettSenereControllerTest {

	@Test
	public void skalKunneGenerereGjenopptaUrl() {
		Assert.assertEquals("http://a34duvw22583.devillo.no:8181/sendsoknad/soknad/abc-123-def#/soknad",
				ServerUtils.getGjenopptaUrl("http://a34duvw22583.devillo.no:8181/sendsoknad/rest/soknad/244/fortsettsenere", "abc-123-def"));
	}
	
}
