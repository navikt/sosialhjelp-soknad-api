package no.nav.sbl.dialogarena.dokumentinnsending.fixture.pages;

import static no.nav.modig.wicket.test.matcher.ComponentMatchers.withId;
import no.nav.modig.wicket.test.FluentWicketTester;
import no.nav.sbl.dialogarena.dokumentinnsending.WicketApplication;

public class OversiktPageObject {

	public void endreTittelPaaVedlegg(FluentWicketTester<WicketApplication> wicketTester, String nyTittel) {
		wicketTester.click()
    	.link(withId("dokumentNavn"));
	}
}
